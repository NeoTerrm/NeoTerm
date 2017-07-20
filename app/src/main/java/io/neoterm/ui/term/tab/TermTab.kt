package io.neoterm.ui.term.tab

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.R
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.preference.NeoPreference
import io.neoterm.terminal.client.TermDataHolder
import io.neoterm.terminal.client.TermUiPresenter
import io.neoterm.ui.term.tab.event.TabCloseEvent
import io.neoterm.ui.term.tab.event.TitleChangedEvent
import io.neoterm.ui.term.tab.event.ToggleFullScreenEvent
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : Tab(title), TermUiPresenter {
    var termData = TermDataHolder()
    var toolbar: Toolbar? = null

    fun updateColorScheme() {
        ColorSchemeManager.applyColorScheme(termData.termView, termData.extraKeysView,
                ColorSchemeManager.getCurrentColorScheme())
    }

    fun cleanup() {
        termData.cleanup()
        toolbar = null
    }

    fun onFullScreenModeChanged(fullScreen: Boolean) {
        // Window token changed, we need to recreate PopupWindow
        resetAutoCompleteStatus()
    }

    fun requireHideIme() {
        val terminalView = termData.termView
        if (terminalView != null) {
            val imm = terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    override fun requireToggleFullScreen() {
        EventBus.getDefault().post(ToggleFullScreenEvent())
    }

    override fun requirePaste() {
        termData.termView?.pasteFromClipboard()
    }

    override fun requireClose() {
        requireHideIme()
        EventBus.getDefault().post(TabCloseEvent(this))
    }

    override fun requireUpdateTitle(title: String?) {
        if (title != null && title.isNotEmpty()) {
            this.title = title
            EventBus.getDefault().post(TitleChangedEvent(title))
            if (NeoPreference.loadBoolean(R.string.key_ui_suggestions, true)) {
                termData.viewClient?.updateSuggestions(title)
            } else {
                termData.viewClient?.removeSuggestions()
            }
        }
    }

    override fun requireOnSessionFinished() {
        termData.viewClient?.sessionFinished = true
    }

    fun resetAutoCompleteStatus() {
        termData.onAutoCompleteListener?.onCleanUp()
        termData.onAutoCompleteListener = null
    }
}
