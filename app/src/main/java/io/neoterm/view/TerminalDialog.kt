package io.neoterm.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.utils.TerminalUtils

/**
 * @author kiva
 */
class TerminalDialog(val context: Context) {

    interface SessionFinishedCallback {
        fun onSessionFinished(dialog:TerminalDialog, finishedSession: TerminalSession?)
    }

    @SuppressLint("InflateParams")
    private var view: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
    private var terminalView: TerminalView
    private var terminalViewClient: BasicViewClient
    private var terminalSessionCallback: BasicSessionCallback
    private var dialog: AlertDialog? = null
    private var terminalSession: TerminalSession? = null
    private var sessionFinishedCallback: SessionFinishedCallback? = null
    private var cancelListener: DialogInterface.OnCancelListener? = null

    init {
        terminalView = view.findViewById(R.id.terminal_view_dialog) as TerminalView
        terminalViewClient = BasicViewClient(terminalView)
        TerminalUtils.setupTerminalView(terminalView, terminalViewClient)

        terminalView.setTerminalViewClient(terminalViewClient)
        terminalSessionCallback = object : BasicSessionCallback(terminalView) {
            override fun onSessionFinished(finishedSession: TerminalSession?) {
                sessionFinishedCallback?.onSessionFinished(this@TerminalDialog, finishedSession)
                super.onSessionFinished(finishedSession)
            }
        }
    }

    fun execute(executablePath: String, arguments: Array<String>?): TerminalDialog {
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

        terminalSession = TerminalUtils.createSession(context, executablePath, arguments, null, null, null, terminalSessionCallback, false)
        terminalView.attachSession(terminalSession)
        return this
    }

    fun onDismiss(cancelListener: DialogInterface.OnCancelListener?): TerminalDialog {
        this.cancelListener = cancelListener
        return this
    }

    fun setTitle(title: String?) : TerminalDialog {
        dialog?.setTitle(title)
        return this
    }

    fun onFinish(finishedCallback: SessionFinishedCallback):TerminalDialog {
        this.sessionFinishedCallback = finishedCallback
        return this
    }

    fun show(title: String?) {
        dialog?.setTitle(title)
        dialog?.show()
    }

    fun dismiss(): TerminalDialog {
        dialog?.dismiss()
        return this
    }
}