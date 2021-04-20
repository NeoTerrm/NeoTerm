package io.neoterm.ui.support

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import io.neoterm.R

/**
 * @author kiva
 */
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_faq)
        setSupportActionBar(findViewById(R.id.faq_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }
}