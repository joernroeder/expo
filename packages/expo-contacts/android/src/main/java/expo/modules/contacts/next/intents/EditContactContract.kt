package expo.modules.contacts.next.intents

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import expo.modules.kotlin.activityresult.AppContextActivityResultContract
import java.io.Serializable

data class EditContactInput(val contactUri: Uri): Serializable

class EditContactContract : AppContextActivityResultContract<EditContactInput, Boolean> {

  override fun createIntent(context: Context, input: EditContactInput): Intent {
    return Intent(Intent.ACTION_EDIT, input.contactUri)
  }

  override fun parseResult(input: EditContactInput, resultCode: Int, intent: Intent?): Boolean {
    return resultCode == android.app.Activity.RESULT_OK
  }
}
