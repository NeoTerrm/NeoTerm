package io.neoterm.ui.customization

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.NeoTermPath
import io.neoterm.customize.font.FontManager
import io.neoterm.utils.TerminalUtils
import io.neoterm.view.BasicSessionCallback
import io.neoterm.view.BasicViewClient
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class CustomizationActivity: AppCompatActivity() {
    lateinit var terminalView: TerminalView
    lateinit var viewClient: BasicViewClient
    lateinit var sessionCallback: BasicSessionCallback
    lateinit var session: TerminalSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_customization)
        val toolbar = findViewById(R.id.custom_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terminalView = findViewById(R.id.terminal_view) as TerminalView
        viewClient = BasicViewClient(terminalView)
        sessionCallback = BasicSessionCallback(terminalView)
        TerminalUtils.setupTerminalView(terminalView, viewClient)
        session = TerminalUtils.createSession(this, "${NeoTermPath.USR_PATH}/bin/applets/echo",
                arrayOf("echo", "Hello NeoTerm."), null, null, sessionCallback, false)
        terminalView.attachSession(session)

        setupSpinner(R.id.custom_font_spinner, FontManager.getFontNames(), FontManager.getCurrentFontName(), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val fontName = parent!!.adapter!!.getItem(position) as String
                terminalView.setTypeface(FontManager.getFont(fontName).getTypeFace())
                FontManager.setCurrentFont(fontName)
            }
        })
    }

    private fun setupSpinner(id: Int, data: List<String>, selected: String, listener: AdapterView.OnItemSelectedListener) {
        val spinner = findViewById(id) as Spinner
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
        spinner.setSelection(if (data.contains(selected)) data.indexOf(selected) else 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        session.finishIfRunning()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}