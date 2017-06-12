package io.neoterm.view.tab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        termTab?.onSessionFinished()
    }

    override fun onClipboardText(session: TerminalSession?, text: String?) {
        if (termView != null) {
            val clipboard = termView!!.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("", text)
        }
    }

    override fun onBell(session: TerminalSession?) {
    }

    override fun onColorsChanged(session: TerminalSession?) {
    }
}