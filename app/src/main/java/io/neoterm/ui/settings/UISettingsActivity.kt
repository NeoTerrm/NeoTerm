package io.neoterm.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatPreferenceActivity
import android.view.MenuItem
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.preference.NeoPreference
import io.neoterm.preference.NeoTermPath
import io.neoterm.frontend.floating.TerminalDialog

/**
 * @author kiva
 */
class UISettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar.title = getString(R.string.ui_settings)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.settings_ui)
        findPreference(getString(R.string.key_ui_suggestions))
                .setOnPreferenceChangeListener({ _, newValue ->
                    if (newValue is Boolean && newValue) {
                        AlertDialog.Builder(this@UISettingsActivity)
                                .setMessage(R.string.installer_install_zsh_required)
                                .setPositiveButton(android.R.string.yes, { _, _ ->
                                    installOhMyZsh()
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .show()
                    }
                    return@setOnPreferenceChangeListener true
                })
    }

    private fun installOhMyZsh() {
        TerminalDialog(this)
                .onFinish(object : TerminalDialog.SessionFinishedCallback {
                    override fun onSessionFinished(dialog: TerminalDialog, finishedSession: TerminalSession?) {
                        if (finishedSession?.exitStatus == 0) {
                            dialog.dismiss()
                            NeoPreference.setLoginShell("zsh")
                        } else {
                            dialog.setTitle(getString(R.string.error))
                        }
                    }
                })
                .execute(NeoTermPath.APT_BIN_PATH, arrayOf("apt", "install", "-y", "oh-my-zsh"))
                .show("Installing oh-my-zsh")
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