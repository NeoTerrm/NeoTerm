package io.neoterm.frontend.floating

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.session.shell.ShellParameter
import io.neoterm.frontend.session.shell.ShellTermSession
import io.neoterm.frontend.session.shell.client.BasicSessionCallback
import io.neoterm.frontend.session.shell.client.BasicViewClient
import io.neoterm.utils.Terminals

typealias DialogSessionFinished = (TerminalDialog, TerminalSession?) -> Unit

/**
 * @author kiva
 */
class TerminalDialog(val context: Context) {
  private val termWindowView = WindowTermView(context)
  private val terminalSessionCallback: BasicSessionCallback
  private var dialog: AlertDialog? = null
  private var terminalSession: TerminalSession? = null
  private var sessionFinishedCallback: DialogSessionFinished? = null
  private var cancelListener: DialogInterface.OnCancelListener? = null

  init {
    termWindowView.setTerminalViewClient(BasicViewClient(termWindowView.terminalView))
    terminalSessionCallback = object : BasicSessionCallback(termWindowView.terminalView) {
      override fun onSessionFinished(finishedSession: TerminalSession?) {
        sessionFinishedCallback?.let { it(this@TerminalDialog, finishedSession) }
        super.onSessionFinished(finishedSession)
      }
    }
  }

  fun execute(executablePath: String, arguments: Array<String>?): TerminalDialog {
    if (terminalSession != null) {
      terminalSession?.finishIfRunning()
    }

    dialog = AlertDialog.Builder(context)
      .setView(termWindowView.rootView)
      .setOnCancelListener {
        terminalSession?.finishIfRunning()
        cancelListener?.onCancel(it)
      }
      .create()

    val parameter = ShellParameter()
      .executablePath(executablePath)
      .arguments(arguments)
      .callback(terminalSessionCallback)
      .systemShell(false)
    terminalSession = Terminals.createSession(context, parameter)
    if (terminalSession is ShellTermSession) {
      (terminalSession as ShellTermSession).exitPrompt = context.getString(R.string.process_exit_prompt_press_back)
    }
    termWindowView.attachSession(terminalSession)
    return this
  }

  fun onDismiss(cancelListener: DialogInterface.OnCancelListener?): TerminalDialog {
    this.cancelListener = cancelListener
    return this
  }

  fun setTitle(title: String?): TerminalDialog {
    dialog?.setTitle(title)
    return this
  }

  fun onFinish(finishedCallback: DialogSessionFinished): TerminalDialog {
    this.sessionFinishedCallback = finishedCallback
    return this
  }

  fun show(title: String?) {
    dialog?.setTitle(title)
    dialog?.setCanceledOnTouchOutside(false)
    dialog?.show()
  }

  fun dismiss(): TerminalDialog {
    dialog?.dismiss()
    return this
  }

  fun imeEnabled(enabled: Boolean): TerminalDialog {
    if (enabled) {
      termWindowView.setInputMethodEnabled(true)
    }
    return this
  }
}