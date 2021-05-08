package io.neoterm.frontend.floating

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.TerminalViewClient
import io.neoterm.utils.Terminals

/**
 * @author kiva
 */
class WindowTermView(val context: Context) {
  @SuppressLint("InflateParams")
  var rootView: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
    private set
  var terminalView: TerminalView = rootView.findViewById<TerminalView>(R.id.terminal_view_dialog)
    private set

  init {
    Terminals.setupTerminalView(terminalView)
  }

  fun setTerminalViewClient(terminalViewClient: TerminalViewClient?) {
    terminalView.setTerminalViewClient(terminalViewClient)
  }

  fun attachSession(terminalSession: TerminalSession?) {
    terminalView.attachSession(terminalSession)
  }

  fun setInputMethodEnabled(enabled: Boolean) {
    terminalView.isFocusable = enabled
    terminalView.isFocusableInTouchMode = enabled
  }
}