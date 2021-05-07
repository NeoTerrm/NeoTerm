package io.neoterm.setup

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.neoterm.App
import io.neoterm.R
import io.neoterm.frontend.config.NeoTermPath
import java.io.File
import java.util.*

/**
 * @author kiva
 */
object SetupHelper {
  fun needSetup(): Boolean {
    val PREFIX_FILE = File(NeoTermPath.USR_PATH)
    return !PREFIX_FILE.isDirectory
  }

  fun setup(
    activity: AppCompatActivity, connection: SourceConnection,
    resultListener: ResultListener
  ) {
    if (!needSetup()) {
      resultListener.onResult(null)
      return
    }

    val prefixFile = File(NeoTermPath.USR_PATH)

    val progress = makeProgressDialog(activity)
    progress.max = 100
    progress.show()

    SetupThread(activity, connection, prefixFile, resultListener, progress)
      .start()
  }

  private fun makeProgressDialog(context: Context): ProgressDialog {
    return makeProgressDialog(context, context.getString(R.string.installer_message))
  }

  fun makeProgressDialog(context: Context, message: String): ProgressDialog {
    val dialog = ProgressDialog(context)
    dialog.setMessage(message)
    dialog.isIndeterminate = false
    dialog.setCancelable(false)
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    return dialog
  }

  fun makeErrorDialog(context: Context, messageId: Int): AlertDialog {
    return makeErrorDialog(context, context.getString(messageId))
  }

  fun makeErrorDialog(context: Context, message: String): AlertDialog {
    return AlertDialog.Builder(context)
      .setTitle(R.string.error)
      .setMessage(message)
      .setPositiveButton(android.R.string.yes, null)
      .setNeutralButton(R.string.show_help) { _, _ -> App.get().openHelpLink() }
      .create()
  }

  fun determineArchName(): String {
    for (androidArch in Build.SUPPORTED_ABIS) {
      when (androidArch) {
        "arm64-v8a" -> return "aarch64"
        "armeabi-v7a" -> return "arm"
        "x86_64" -> return "x86_64"
      }
    }
    throw RuntimeException(
      "Unable to determine arch from Build.SUPPORTED_ABIS =  "
        + Arrays.toString(Build.SUPPORTED_ABIS)
    )
  }
}
