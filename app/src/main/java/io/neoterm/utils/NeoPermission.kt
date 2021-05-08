package io.neoterm.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * @author kiva
 */
object NeoPermission {
  const val REQUEST_APP_PERMISSION = 10086

  fun initAppPermission(context: AppCompatActivity, requestCode: Int) {
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
      )
      != PackageManager.PERMISSION_GRANTED
    ) {

      if (ActivityCompat.shouldShowRequestPermissionRationale(
          context,
          Manifest.permission.READ_EXTERNAL_STORAGE
        )
      ) {
        AlertDialog.Builder(context).setMessage("需要存储权限来访问存储设备上的文件")
          .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
            doRequestPermission(context, requestCode)
          })
          .show()

      } else {
        doRequestPermission(context, requestCode)
      }
    }
  }

  private fun doRequestPermission(context: AppCompatActivity, requestCode: Int) {
    try {
      ActivityCompat.requestPermissions(
        context,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        requestCode
      )
    } catch (ignore: ActivityNotFoundException) {
      // for MIUI, we ignore it.
    }
  }
}
