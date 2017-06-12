package io.neoterm.view.tab

import io.neoterm.backend.TerminalSession
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class TermSessionChangedCallback : TerminalSession.SessionChangedCallback {
    var termView: TerminalView? = null
    var termTab: TermTab? = null

    override fun onTextChanged(changedSession: TerminalSession?) {
        termView?.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession?) {
        if (changedSession?.title != null) {
            termTab?.updateTitle(changedSession.title)
        }
    }

    override fun onSessionFinished(finishedSession: TerminalSession?) {

    }

    override fun onClipboardText(session: TerminalSession?, text: String?) {
    }

    override fun onBell(session: TerminalSession?) {
    }

    override fun onColorsChanged(session: TerminalSession?) {
    }
}