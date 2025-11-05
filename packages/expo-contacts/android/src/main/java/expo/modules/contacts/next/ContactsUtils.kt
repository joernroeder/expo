package expo.modules.contacts.next

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import expo.modules.contacts.next.domain.wrappers.ContactId
import expo.modules.contacts.next.domain.wrappers.RawContactId

fun ContentResolver.safeDelete(
  uri: Uri,
  where: String? = null,
  selectionArgs: Array<String>? = null
): Int {
  return try {
    delete(uri, where, selectionArgs)
  } catch (e: SecurityException) {
    throw PermissionException(Manifest.permission.WRITE_CONTACTS, e)
  }
}

fun ContentResolver.safeQuery(
  uri: Uri,
  projection: Array<String>,
  selection: String? = null,
  selectionArgs: Array<String>? = null,
  sortOrder: String? = null
): Cursor {
  return try {
    query(uri, projection, selection, selectionArgs, sortOrder) ?:
      throw CouldNotExecuteQueryException("Cursor returned by query is null")
  } catch (e: SecurityException) {
    throw PermissionException(Manifest.permission.READ_CONTACTS, e)
  }
}

fun ContentResolver.safeApplyBatch(
  authority: String,
  operations: ArrayList<ContentProviderOperation>
): Array<ContentProviderResult> {
  return try {
    applyBatch(authority, operations)
  } catch (e: SecurityException) {
    throw PermissionException(Manifest.permission.WRITE_CONTACTS, e)
  }
}

fun ContentProviderOperation.Builder.withValueIfNotNull(key: String, value: Any?) = apply {
  if (value != null) {
    withValue(key, value)
  }
}

fun ContentResolver.getContactIdFromRawContactId(rawContactId: RawContactId): ContactId {
  val projection = arrayOf(ContactsContract.RawContacts.CONTACT_ID)
  val selection = "${ContactsContract.RawContacts._ID} = ?"
  val selectionArgs = arrayOf(rawContactId.value)

  val cursor = safeQuery(
    ContactsContract.RawContacts.CONTENT_URI,
    projection,
    selection,
    selectionArgs,
  )

  cursor.use { c ->
    if (c.moveToFirst()) {
      val contactIdValue = c.getString(c.getColumnIndexOrThrow(ContactsContract.RawContacts.CONTACT_ID))
      return ContactId(contactIdValue)
    } else {
      throw IllegalStateException("Cannot find contactId for the given rawContactId: $rawContactId")
    }
  }
}
