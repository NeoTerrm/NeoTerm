package io.neoterm.frontend.client

/**
 * @author kiva
 */
interface TermUiPresenter {
    fun requireClose()
    fun requireToggleFullScreen()
    fun requirePaste()
    fun requireUpdateTitle(title: String?)
    fun requireOnSessionFinished()
}