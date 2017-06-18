package io.neoterm.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatPreferenceActivity
import android.view.MenuItem
import io.neoterm.R

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
                .setOnPreferenceChangeListener({_, newValue ->
                    if (newValue as Boolean) {
                        AlertDialog.Builder(this@UISettingsActivity)
                                .setMessage(R.string.installer_install_zsh_manually)
                                .setPositiveButton(android.R.string.yes, null)
                                .show()
                    }
                    return@setOnPreferenceChangeListener true
                })
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