package io.neoterm.ui.term.tab

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.preference.NeoPreference
import io.neoterm.ui.term.tab.event.TabCloseEvent
import io.neoterm.ui.term.tab.event.TitleChangedEvent
import io.neoterm.ui.term.tab.event.ToggleFullScreenEvent
import io.neoterm.view.OnAutoCompleteListener
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : Tab(title) {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionChangedCallback? = null
    var viewClient: TermViewClient? = null
    var onAutoCompleteListener: OnAutoCompleteListener? = null
    var toolbar: Toolbar? = null

    fun updateColorScheme() {
        ColorSchemeManager.applyColorScheme(viewClient?.termView, viewClient?.extraKeysView,
                ColorSchemeManager.getCurrentColorScheme())
    }

    fun cleanup() {
        onAutoCompleteListener?.onCleanUp()
        onAutoCompleteListener = null

        viewClient?.termTab = null
        viewClient?.termView = null
        viewClient?.extraKeysView = null
        sessionCallback?.termView = null
        sessionCallback?.termTab = null
        toolbar = null
        termSession = null
    }

    fun updateTitle(title: String) {
        if (title.isNotEmpty()) {
            this.title = title
            EventBus.getDefault().post(TitleChangedEvent(title))
            if (NeoPreference.loadBoolean(R.string.key_ui_suggestions, true)) {
                viewClient?.updateSuggestions(title)
            } else {
                viewClient?.removeSuggestions()
            }
        }
    }

    fun onSessionFinished() {
        viewClient?.sessionFinished = true
    }

    fun onFullScreenModeChanged(fullScreen: Boolean) {
        // Window token changed, we need to recreate PopupWindow
        onAutoCompleteListener?.onCleanUp()
        onAutoCompleteListener = null
    }

    fun requireCloseTab() {
        requireHideIme()
        EventBus.getDefault().post(TabCloseEvent(this))
    }

    fun requireHideIme() {
        val terminalView = viewClient?.termView
        if (terminalView != null) {
            val imm = terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    fun requireToggleFullScreen() {
        EventBus.getDefault().post(ToggleFullScreenEvent())
    }

    fun requirePaste() {
        viewClient?.termView?.pasteFromClipboard()
    }
}
