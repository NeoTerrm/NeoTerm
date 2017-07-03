package io.neoterm.preference

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * @author kiva
 */
object NeoPermission {
    const val REQUEST_APP_PERMISSION = 10086

    fun initAppPermission(context: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(context).setMessage("需要存储权限来访问存储设备上的文件")
                        .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                            ActivityCompat.requestPermissions(context,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    requestCode)
                        })
                        .show()

            } else {
                ActivityCompat.requestPermissions(context,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        requestCode)
            }
        }
    }
}