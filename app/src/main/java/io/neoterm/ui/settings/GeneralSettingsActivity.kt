package io.neoterm.ui.settings

import android.os.Bundle
import android.support.v7.app.AppCompatPreferenceActivity
import android.view.MenuItem
import io.neoterm.R

/**
 * @author kiva
 */
class GeneralSettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar.title = getString(R.string.general_settings)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.setting_general)
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