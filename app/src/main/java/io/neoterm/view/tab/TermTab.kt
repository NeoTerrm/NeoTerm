package io.neoterm.view.tab

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.customize.color.NeoColorScheme
import io.neoterm.preference.NeoPreference
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : Tab(title) {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionChangedCallback? = null
    var viewClient: TermViewClient? = null
    var toolbar: Toolbar? = null

    fun updateColorScheme() {
        ColorSchemeManager.applyColorScheme(viewClient?.termView, viewClient?.extraKeysView,
                ColorSchemeManager.getCurrentColorScheme())
    }

    fun cleanup() {
        viewClient?.termTab = null
        viewClient?.termView = null
        viewClient?.extraKeysView = null
        sessionCallback?.termView = null
        sessionCallback?.termTab = null
        toolbar = null
        termSession = null
    }

    fun updateTitle(title: String) {
        this.title = title
        toolbar?.title = title
        if (NeoPreference.loadBoolean(R.string.key_ui_suggestions, true)) {
            viewClient?.updateSuggestions(title)
        } else {
            viewClient?.removeSuggestions()
        }
    }

    fun onSessionFinished() {
        viewClient?.sessionFinished = true
    }

    fun requiredCloseTab() {
        hideIme()
        EventBus.getDefault().post(TabCloseEvent(this))
    }

    fun hideIme() {
        val terminalView = viewClient?.termView
        if (terminalView != null) {
            val imm = terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}
