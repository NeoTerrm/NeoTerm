package io.neoterm.frontend.session.shell.client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.session.shell.ShellTermSession

/**
 * @author kiva
 */
class TermSessionCallback : TerminalSession.SessionChangedCallback {
  var termSessionData: TermSessionData? = null

  var bellController: BellController? = null

  override fun onTextChanged(changedSession: TerminalSession?) {
    termSessionData?.termView?.onScreenUpdated()
  }

  override fun onTitleChanged(changedSession: TerminalSession?) {
    if (changedSession?.title != null) {
      termSessionData?.termUI?.requireUpdateTitle(changedSession.title)
    }
  }

  override fun onSessionFinished(finishedSession: TerminalSession?) {
    termSessionData?.termUI?.requireOnSessionFinished()
  }

  override fun onClipboardText(session: TerminalSession?, text: String?) {
    val termView = termSessionData?.termView
    if (termView != null) {
      val clipboard = termView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipboard.primaryClip = ClipData.newPlainText("", text)
    }
  }

  override fun onBell(session: TerminalSession?) {
    val termView = termSessionData?.termView ?: return
    val shellSession = session as ShellTermSession

    if (bellController == null) {
      bellController = BellController()
    }

    bellController?.bellOrVibrate(termView.context, shellSession)
  }

  override fun onColorsChanged(session: TerminalSession?) {
    val termView = termSessionData?.termView
    if (session != null && termView != null) {
      termView.onScreenUpdated()
    }
  }
}