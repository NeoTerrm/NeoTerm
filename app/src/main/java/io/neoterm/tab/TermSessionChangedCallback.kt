package io.neoterm.tab

import io.neoterm.terminal.TerminalSession
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class TermSessionChangedCallback : TerminalSession.SessionChangedCallback {
    var termView: TerminalView? = null

    override fun onTextChanged(changedSession: TerminalSession?) {
        termView?.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession?) {

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