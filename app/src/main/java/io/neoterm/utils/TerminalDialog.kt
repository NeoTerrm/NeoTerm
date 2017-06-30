package io.neoterm.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.preference.NeoPreference
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

/**
 * @author kiva
 */
class TerminalDialog(val context: Context, var cancelListener: DialogInterface.OnCancelListener?) {
    class MinimalViewClient : TerminalViewClient {
        override fun onScale(scale: Float): Float {
            return scale
        }

        override fun onSingleTapUp(e: MotionEvent?) {
        }

        override fun shouldBackButtonBeMappedToEscape(): Boolean {
            return false
        }

        override fun copyModeChanged(copyMode: Boolean) {
        }

        override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
            return false
        }

        override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
            return false
        }

        override fun readControlKey(): Boolean {
            return false
        }

        override fun readAltKey(): Boolean {
            return false
        }

        override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
            return false
        }

        override fun onLongPress(event: MotionEvent?): Boolean {
            return false
        }
    }

    class MinimalSessionCallback(var terminalView: TerminalView) : TerminalSession.SessionChangedCallback {
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

    var view: View
    var terminalView: TerminalView
    var terminalViewClient: MinimalViewClient
    var terminalSessionCallback: MinimalSessionCallback
    var dialog: AlertDialog? = null
    var terminalSession: TerminalSession? = null

    init {
        view = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)

        terminalView = view.findViewById(R.id.terminal_view_dialog) as TerminalView
        terminalView.textSize = NeoPreference.loadInt(NeoPreference.KEY_FONT_SIZE, 30)
        terminalView.setTypeface(Typeface.MONOSPACE)

        terminalViewClient = MinimalViewClient()
        terminalView.setOnKeyListener(terminalViewClient)
        terminalSessionCallback = MinimalSessionCallback(terminalView)
    }

    fun execute(executablePath: String, arguments: Array<String>?) {
        if (terminalSession != null) {
            terminalSession?.finishIfRunning()
        }

        dialog = AlertDialog.Builder(context)
                .setView(view)
                .setOnCancelListener {
                    terminalSession?.finishIfRunning()
                    cancelListener?.onCancel(it)
                }
                .create()

        terminalSession = TerminalUtils.createSession(context, executablePath, arguments, null, null, terminalSessionCallback, false)
        terminalView.attachSession(terminalSession)
    }

    fun show(title: String?) {
        dialog?.setTitle(title)
        dialog?.show()
    }

}