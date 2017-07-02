package io.neoterm.ui.customization

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.NeoTermPath
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