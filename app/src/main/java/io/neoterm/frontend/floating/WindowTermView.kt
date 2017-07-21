package io.neoterm.frontend.floating

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.utils.TerminalUtils
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

/**
 * @author kiva
 */
class WindowTermView(val context: Context) {
    @SuppressLint("InflateParams")
    var rootView: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
        private set
    var terminalView: TerminalView = rootView.findViewById(R.id.terminal_view_dialog) as TerminalView
        private set

    init {
        TerminalUtils.setupTerminalView(terminalView)
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