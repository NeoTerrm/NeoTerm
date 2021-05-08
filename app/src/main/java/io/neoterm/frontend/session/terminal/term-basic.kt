package io.neoterm.frontend.session.terminal

import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import io.neoterm.backend.TerminalSession
import io.neoterm.component.config.NeoPreference
import io.neoterm.frontend.session.view.TerminalView
import io.neoterm.frontend.session.view.TerminalViewClient

open class BasicSessionCallback(var terminalView: TerminalView) : TerminalSession.SessionChangedCallback {
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
    if (session != null) {
      terminalView.onScreenUpdated()
    }
  }
}

class BasicViewClient(val terminalView: TerminalView) : TerminalViewClient {
  override fun onScale(scale: Float): Float {
    if (scale < 0.9f || scale > 1.1f) {
      val increase = scale > 1f
      val changedSize = (if (increase) 1 else -1) * 2
      val fontSize = NeoPreference.validateFontSize(terminalView.textSize + changedSize)
      terminalView.textSize = fontSize
      return 1.0f
    }
    return scale
  }

  override fun onSingleTapUp(e: MotionEvent?) {
    if (terminalView.isFocusable && terminalView.isFocusableInTouchMode) {
      (terminalView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(terminalView, InputMethodManager.SHOW_IMPLICIT)
    }
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
