package io.neoterm.frontend.client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.SoundPool
import android.os.Vibrator
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.preference.NeoPreference

/**
 * @author kiva
 */
class TermSessionCallback : TerminalSession.SessionChangedCallback {
    var termData: TermDataHolder? = null

    var bellId: Int = 0
    var soundPool: SoundPool? = null

    override fun onTextChanged(changedSession: TerminalSession?) {
        termData?.termView?.onScreenUpdated()
    }

    override fun onTitleChanged(changedSession: TerminalSession?) {
        if (changedSession?.title != null) {
            termData?.termUI?.requireUpdateTitle(changedSession.title)
        }
    }

    override fun onSessionFinished(finishedSession: TerminalSession?) {
        termData?.termUI?.requireOnSessionFinished()
    }

    override fun onClipboardText(session: TerminalSession?, text: String?) {
        val termView = termData?.termView
        if (termView != null) {
            val clipboard = termView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("", text)
        }
    }

    override fun onBell(session: TerminalSession?) {
        val termView = termData?.termView ?: return

        if (NeoPreference.loadBoolean(R.string.key_general_bell, false)) {
            if (soundPool == null) {
                soundPool = SoundPool.Builder().setMaxStreams(1).build()
                bellId = soundPool!!.load(termView!!.context, R.raw.bell, 1)
            }
            soundPool?.play(bellId, 1f, 1f, 0, 0, 1f)
        }

        if (NeoPreference.loadBoolean(R.string.key_general_vibrate, false)) {
            val vibrator = termView!!.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
    }

    override fun onColorsChanged(session: TerminalSession?) {
        val termView = termData?.termView
        if (session != null && termView != null) {
            termView.onScreenUpdated()
        }
    }
}