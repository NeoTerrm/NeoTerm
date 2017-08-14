package io.neoterm.ui.term.tab

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.frontend.client.TermDataHolder
import io.neoterm.frontend.client.TermUiPresenter
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.ui.term.event.TabCloseEvent
import io.neoterm.ui.term.event.TitleChangedEvent
import io.neoterm.ui.term.event.ToggleFullScreenEvent
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : Tab(title), TermUiPresenter {
    var termData = TermDataHolder()
    var toolbar: Toolbar? = null

    fun updateColorScheme() {
        val colorSchemeManager = ComponentManager.getComponent<ColorSchemeComponent>()
        colorSchemeManager.applyColorScheme(termData.termView, termData.extraKeysView,
                colorSchemeManager.getCurrentColorScheme())
    }

    fun cleanup() {
        termData.cleanup()
        toolbar = null
    }

    fun onFullScreenModeChanged(fullScreen: Boolean) {
        // Window token changed, we need to recreate PopupWindow
        resetAutoCompleteStatus()
    }

    override fun requireHideIme() {
        val terminalView = termData.termView
        if (terminalView != null) {
            val imm = terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(terminalView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    override fun requireFinishAutoCompletion(): Boolean {
        return termData.onAutoCompleteListener?.onFinishCompletion() ?: false
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
            termData.viewClient?.updateSuggestions(title)
        }
    }

    override fun requireOnSessionFinished() {
        termData.viewClient?.sessionFinished = true
    }

    fun resetAutoCompleteStatus() {
        termData.onAutoCompleteListener?.onCleanUp()
        termData.onAutoCompleteListener = null
    }

    fun resetStatus() {
        resetAutoCompleteStatus()
        termData.extraKeysView?.updateButtons()
        termData.termView?.updateSize()
        termData.termView?.onScreenUpdated()
    }
}
