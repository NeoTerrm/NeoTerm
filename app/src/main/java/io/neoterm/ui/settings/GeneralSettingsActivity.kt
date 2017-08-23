package io.neoterm.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import io.neoterm.R
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.utils.PackageUtils

/**
 * @author kiva
 */
class GeneralSettingsActivity : BasePreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.general_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.setting_general)

        val currentShell = NeoPreference.loadString(R.string.key_general_shell, "sh")
        findPreference(getString(R.string.key_general_shell)).setOnPreferenceChangeListener { _, value ->
            val shellName = value.toString()
            val newShell = NeoPreference.findLoginProgram(shellName)
            if (newShell == null) {
                requestInstallShell(shellName, currentShell)
            } else {
                postChangeShell(shellName)
            }
            return@setOnPreferenceChangeListener true
        }
    }

    private fun postChangeShell(shellName: String) {
        NeoPreference.setLoginShell(shellName)
    }

    private fun requestInstallShell(shellName: String, currentShell: String) {
        var selectedShell = currentShell
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.shell_not_found, shellName))
                .setMessage(R.string.shell_not_found_message)
                .setPositiveButton(R.string.install, { _, _ ->
                    PackageUtils.executeApt(this, "install", arrayOf("-y"), { exitStatus, dialog ->
                        if (exitStatus == 0) {
                            dialog.dismiss()
                            selectedShell = shellName
                        } else {
                            dialog.setTitle(getString(R.string.error))
                        }
                    })
                })
                .setNegativeButton(android.R.string.no, null)
                .setOnDismissListener {
                    postChangeShell(selectedShell)
                }
                .show()
    }

    override fun onBuildHeaders(target: MutableList<Header>?) {
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }
}