package io.neoterm.view

import io.neoterm.backend.TerminalSession

/**
 * @author kiva
 */
class BasicSessionCallback(var terminalView: TerminalView) : TerminalSession.SessionChangedCallback {
    override fun onTextChanged(changedSession: TerminalSession?) {
        if (changedSession != null) {
            terminalView.onScreenUpdated()
        }
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
