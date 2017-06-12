package io.neoterm.ui.settings

import android.os.Bundle
import android.support.v7.app.AppCompatPreferenceActivity
import android.view.MenuItem
import io.neoterm.R

/**
 * @author kiva
 */
class PackageSettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar.title = getString(R.string.package_settings)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        addPreferencesFromResource(R.xml.settings_package)
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