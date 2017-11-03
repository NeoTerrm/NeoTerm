package io.neoterm.ui.setup

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import io.neoterm.App
import io.neoterm.R
import io.neoterm.component.setup.BaseFileInstaller
import io.neoterm.utils.PackageUtils


/**
 * @author kiva
 */
class SetupActivity : AppCompatActivity() {
    private var aptUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_setup)
        installBaseFiles()
    }

    private fun installBaseFiles() {
        var resultListener: BaseFileInstaller.ResultListener? = null
        resultListener = BaseFileInstaller.ResultListener { error ->
            if (error == null) {
                setResult(Activity.RESULT_OK)
                PackageUtils.syncSource()
                executeAptUpdate()
            } else {
                AlertDialog.Builder(this@SetupActivity)
                        .setTitle(R.string.error)
                        .setMessage(error.toString())
                        .setNegativeButton(R.string.use_system_shell, { _, _ ->
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        })
                        .setPositiveButton(R.string.retry, { dialog, _ ->
                            dialog.dismiss()
                            BaseFileInstaller.installBaseFiles(this@SetupActivity, resultListener)
                        })
                        .setNeutralButton(R.string.show_help, { _, _ ->
                            App.get().openHelpLink()
                        })
                        .show()
            }
        }
        BaseFileInstaller.installBaseFiles(this, resultListener)
    }

    private fun executeAptUpdate() {
        PackageUtils.executeApt(this, "update", null, { exitStatus, dialog ->
            if (exitStatus == 0) {
                dialog.dismiss()
                aptUpdated = true
                executeAptUpgrade()
            } else {
                dialog.setTitle(getString(R.string.error))
            }
        })
    }

    private fun executeAptUpgrade() {
        PackageUtils.executeApt(this, "upgrade", arrayOf("-y"), { exitStatus, dialog ->
            if (exitStatus == 0) {
                dialog.dismiss()
                finish()
            } else {
                dialog.setTitle(getString(R.string.error))
            }
        })
    }
}