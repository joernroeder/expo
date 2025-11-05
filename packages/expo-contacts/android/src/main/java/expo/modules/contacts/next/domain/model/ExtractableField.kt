package expo.modules.contacts.next.domain.model

import android.database.Cursor

// To extract data from DATA table following information is required:
// - projection - indexes of columns to extract (every type has their own protocol how to save data to the database)
// - mimeType - by this value the cursor rows are filtered
// - one query returns
// - extract - this function describes how to convert cursor row to the result
interface ExtractableField<T: Extractable> {
  val projection: Array<String>
  val mimeType: String
  fun extract(cursor: Cursor): T
}