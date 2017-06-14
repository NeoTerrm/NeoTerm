package io.neoterm.view.tab

import android.graphics.Color
import android.support.v7.widget.Toolbar
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.color.NeoTermColorScheme
import io.neoterm.preference.NeoTermPreference

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : Tab(title) {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionChangedCallback? = null
    var viewClient: TermViewClient? = null
    var toolbar: Toolbar? = null

    var closeTabProvider: CloseTabProvider? = null

    fun changeColorScheme(colorScheme: NeoTermColorScheme?) {
        colorScheme?.apply()
        viewClient?.extraKeysView?.setBackgroundColor(Color.parseColor(colorScheme?.background))
    }

    fun cleanup() {
        viewClient?.termTab = null
        viewClient?.termView = null
        viewClient?.extraKeysView = null
        sessionCallback?.termView = null
        sessionCallback?.termTab = null
        closeTabProvider = null
        toolbar = null
        termSession = null
    }

    fun updateTitle(title: String) {
        this.title = title
        toolbar?.title = title
        if (NeoTermPreference.loadBoolean(R.string.key_ui_suggestions, true)) {
            viewClient?.updateSuggestions(title)
        }
    }

    fun onSessionFinished() {
        viewClient?.sessionFinished = true
    }

    fun requiredCloseTab() {
        closeTabProvider?.closeTab(this)
    }
}
