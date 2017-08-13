package io.neoterm.ui.customize

import android.os.Bundle
import android.view.MenuItem
import io.neoterm.R

/**
 * @author kiva
 */
class ColorSchemeActivity : BaseCustomizeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCustomizationComponent(R.layout.ui_color_scheme)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}