package io.neoterm.ui.term.tab

import android.content.Context
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.session.shell.client.TermSessionData
import io.neoterm.frontend.session.shell.client.TermUiPresenter
import io.neoterm.frontend.session.shell.client.event.*
import org.greenrobot.eventbus.EventBus

/**
 * @author kiva
 */

class TermTab(title: CharSequence) : NeoTab(title), TermUiPresenter {
    companion object {
        val PARAMETER_SHOW_EKS = "show_eks"
    }

    var termData = TermSessionData()
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
            termData.viewClient?.updateExtraKeys(title)
        }
    }

    override fun requireOnSessionFinished() {
        // do nothing
    }

    override fun requireCreateNew() {
        EventBus.getDefault().post(CreateNewSessionEvent())
    }

    override fun requireSwitchToPrevious() {
        EventBus.getDefault().post(SwitchSessionEvent(toNext = false))
    }

    override fun requireSwitchToNext() {
        EventBus.getDefault().post(SwitchSessionEvent(toNext = true))
    }

    override fun requireSwitchTo(index: Int) {
        EventBus.getDefault().post(SwitchIndexedSessionEvent(index))
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
