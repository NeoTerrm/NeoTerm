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
class TerminalDialog(val context: Context, var cancelListener: DialogInterface.OnCancelListener?) {

    @SuppressLint("InflateParams")
    var view: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
    var terminalView: TerminalView
    var terminalViewClient: BasicViewClient
    var terminalSessionCallback: BasicSessionCallback
    var dialog: AlertDialog? = null
    var terminalSession: TerminalSession? = null

    init {
        terminalView = view.findViewById(R.id.terminal_view_dialog) as TerminalView
        terminalViewClient = BasicViewClient(terminalView)
        TerminalUtils.setupTerminalView(terminalView, terminalViewClient)

        terminalView.setOnKeyListener(terminalViewClient)
        terminalSessionCallback = BasicSessionCallback(terminalView)
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