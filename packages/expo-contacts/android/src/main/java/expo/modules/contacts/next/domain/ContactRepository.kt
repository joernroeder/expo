package expo.modules.contacts.next.domain

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentUris
import android.provider.ContactsContract
import expo.modules.contacts.next.UnableToExtractIdFromUriException
import expo.modules.contacts.next.domain.model.Appendable
import expo.modules.contacts.next.domain.model.Extractable
import expo.modules.contacts.next.domain.model.ExtractableField
import expo.modules.contacts.next.domain.model.Patchable
import expo.modules.contacts.next.domain.model.Updatable
import expo.modules.contacts.next.domain.model.contact.ContactPatch
import expo.modules.contacts.next.domain.model.contact.ExistingContact
import expo.modules.contacts.next.domain.model.contact.NewContact
import expo.modules.contacts.next.domain.query.QueryAggregator
import expo.modules.contacts.next.domain.query.QueryBuilder
import expo.modules.contacts.next.domain.wrappers.ContactId
import expo.modules.contacts.next.domain.wrappers.DataId
import expo.modules.contacts.next.domain.wrappers.RawContactId
import expo.modules.contacts.next.getContactIdFromRawContactId
import expo.modules.contacts.next.safeApplyBatch
import expo.modules.contacts.next.safeDelete
import expo.modules.contacts.next.safeQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository(val contentResolver: ContentResolver) {
  suspend fun insert(contact: NewContact): ContactId = withContext(Dispatchers.IO) {
    val operations = contact.toInsertOperations()
    val result = contentResolver.safeApplyBatch(ContactsContract.AUTHORITY, operations)
    val rawContactId = RawContactId(extractId(result))
    return@withContext contentResolver.getContactIdFromRawContactId(rawContactId)
  }

  suspend fun patch(contactPatch: ContactPatch): Boolean = withContext(Dispatchers.IO) {
    val operations = contactPatch.toPatchOperations()
    contentResolver.safeApplyBatch(ContactsContract.AUTHORITY, operations)
    return@withContext true
  }

  suspend fun delete(contactId: ContactId): Boolean = withContext(Dispatchers.IO) {
    val uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, contactId.value.toLong())
    val rowsDeleted = contentResolver.safeDelete(uri)
    return@withContext rowsDeleted > 0
  }

  suspend fun appendField(appendable: Appendable): DataId =
    withContext(Dispatchers.IO) {
      val operation = appendable.toAppendOperation()
      val result = contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
      val id = extractId(result)
      return@withContext DataId(id)
    }

  suspend fun updateField(updatable: Updatable): Boolean = withContext(Dispatchers.IO) {
    val operation = updatable.toUpdateOperation()
    contentResolver.safeApplyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    true
  }

  suspend fun patchField(patchable: Patchable): Boolean = withContext(Dispatchers.IO) {
    val operation = patchable.toPatchOperation()
    contentResolver.safeApplyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    true
  }

  suspend fun deleteField(dataId: DataId): Boolean = withContext(Dispatchers.IO) {
    val operation = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
      .withSelection("${DataId.Companion.COLUMN_IN_DATA_TABLE} = ?", arrayOf(dataId.value))
      .build()
    contentResolver.safeApplyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    return@withContext true
  }

  suspend fun getAllIds(): List<ContactId> = withContext(Dispatchers.IO) {
    val ids = mutableListOf<ContactId>()
    contentResolver.safeQuery(
      uri = ContactsContract.Data.CONTENT_URI,
      projection = arrayOf(ContactId.Companion.COLUMN_IN_DATA_TABLE)
    ).use { cursor ->
      while (cursor.moveToNext()) {
        ids.add(ContactId(cursor.getString(0)))
      }
    }
    return@withContext ids
  }

  suspend fun getContactWithDetails(
    extractableFields: Set<ExtractableField<*>>,
    contactId: ContactId
  ): ExistingContact = withContext(Dispatchers.IO) {
    contentResolver.safeQuery(
      uri = ContactsContract.Data.CONTENT_URI,
      projection = QueryBuilder.buildProjection(extractableFields),
      selection = "${ContactId.COLUMN_IN_DATA_TABLE} = ?",
      selectionArgs = arrayOf(contactId.value)
    ).use { cursor ->
      return@withContext QueryAggregator.aggregateOne(cursor, extractableFields, contactId)
    }
  }

  suspend fun getContactsWithDetails(
    extractors: Set<ExtractableField<*>>,
  ): Collection<ExistingContact> = withContext(Dispatchers.IO) {
    contentResolver.safeQuery(
      uri = ContactsContract.Data.CONTENT_URI,
      projection = QueryBuilder.buildProjection(extractors),
    ).use { cursor ->
      return@withContext QueryAggregator.aggregate(cursor, extractors)
    }
  }

  suspend fun getLookupKey(contactId: ContactId): String? = withContext(Dispatchers.IO) {
    contentResolver.safeQuery(
      uri = ContactsContract.Contacts.CONTENT_URI,
      projection = arrayOf(ContactsContract.Contacts.LOOKUP_KEY),
      selection = "${ContactId.COLUMN_IN_CONTACTS_TABLE} = ?",
      selectionArgs = arrayOf(contactId.value)
    ).use { cursor ->
      return@withContext if (cursor.moveToFirst()) {
        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
      } else {
        null
      }
    }
  }

  suspend fun getRawContactId(
    contactId: ContactId,
    accountType: String? = null,
    accountName: String? = null
  ): RawContactId? = withContext(Dispatchers.IO) {
    val selectionBuilder = StringBuilder("${ContactsContract.RawContacts.CONTACT_ID}=?")
    val args = mutableListOf(contactId.value)

    if (accountType == null || accountName == null) {
      selectionBuilder.append(" AND ${ContactsContract.RawContacts.ACCOUNT_TYPE} IS NULL")
      selectionBuilder.append(" AND ${ContactsContract.RawContacts.ACCOUNT_NAME} IS NULL")
    } else {
      selectionBuilder.append(" AND ${ContactsContract.RawContacts.ACCOUNT_TYPE}=?")
      selectionBuilder.append(" AND ${ContactsContract.RawContacts.ACCOUNT_NAME}=?")
      args.add(accountType)
      args.add(accountName)
    }

    contentResolver.safeQuery(
      uri = ContactsContract.RawContacts.CONTENT_URI,
      projection = arrayOf(RawContactId.COLUMN_IN_RAW_CONTACTS_TABLE),
      selection = selectionBuilder.toString(),
      selectionArgs = args.toTypedArray(),
      sortOrder = ContactsContract.RawContacts._ID + " ASC"
    ).use { cursor ->
      if (cursor.moveToFirst()) {
        RawContactId(
          cursor.getString(
            cursor.getColumnIndexOrThrow(RawContactId.COLUMN_IN_RAW_CONTACTS_TABLE)
          )
        )
      } else {
        null
      }
    }
  }


  suspend fun <T: Extractable> getById(
    extractableField: ExtractableField<T>,
    contactId: ContactId
  ): List<T> = withContext(Dispatchers.IO) {
    contentResolver.safeQuery(
      uri = ContactsContract.Data.CONTENT_URI,
      projection = extractableField.projection,
      selection = "${ContactId.COLUMN_IN_DATA_TABLE} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
      selectionArgs = arrayOf(contactId.value, extractableField.mimeType)
    ).use { cursor ->
      QueryAggregator.aggregateOneField(cursor, extractableField)
    }
  }

  private fun extractId(result: Array<ContentProviderResult>): String {
    val uri = requireNotNull(result[0].uri)
    return uri.lastPathSegment
      ?: throw UnableToExtractIdFromUriException(uri)
  }
}