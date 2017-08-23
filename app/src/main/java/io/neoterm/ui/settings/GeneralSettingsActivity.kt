package io.neoterm.ui.settings

import android.os.Bundle
import android.view.MenuItem
import io.neoterm.R
import io.neoterm.frontend.preference.NeoPreference

/**
 * @author kiva
 */
class GeneralSettingsActivity : BasePreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.general_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.setting_general)
        findPreference(getString(R.string.key_general_shell)).setOnPreferenceChangeListener { _, value ->
            val shellName = value.toString()
            val newShell = NeoPreference.findLoginProgram(shellName)
            if (newShell == null) {
                requestInstallShell(shellName)
            } else {
                postChangeShell(shellName)
            }
            return@setOnPreferenceChangeListener true
        }
    }

    private fun postChangeShell(shellName: String) {
        NeoPreference.store(R.string.key_general_shell, shellName)
    }

    private fun requestInstallShell(shellName: String) {

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