package io.neoterm.frontend.session.shell.client

/**
 * @author kiva
 */
interface TermUiPresenter {
    fun requireClose()
    fun requireToggleFullScreen()
    fun requirePaste()
    fun requireUpdateTitle(title: String?)
    fun requireOnSessionFinished()
    fun requireHideIme()
    fun requireFinishAutoCompletion(): Boolean
    fun requireCreateNew()
    fun requireSwitchToPrevious()
    fun requireSwitchToNext()
    fun requireSwitchTo(index: Int)
}