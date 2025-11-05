package expo.modules.contacts.next.domain.query

import android.provider.ContactsContract
import expo.modules.contacts.next.domain.model.ExtractableField
import expo.modules.contacts.next.domain.wrappers.ContactId
import expo.modules.contacts.next.domain.wrappers.DataId

class QueryBuilder() {
  companion object {
    fun buildProjection(extractors: Set<ExtractableField<*>>): Array<String> {
      val requiredColumns = listOf(
        ContactId.COLUMN_IN_DATA_TABLE,
        DataId.COLUMN_IN_DATA_TABLE,
        ContactsContract.Data.MIMETYPE
      )
      return extractors
        .flatMap { it.projection.toList() }
        .toSet()
        .plus(requiredColumns)
        .toTypedArray()
    }
  }
}
