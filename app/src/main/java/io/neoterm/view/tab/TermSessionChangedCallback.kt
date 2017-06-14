package io.neoterm.view.tab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.SoundPool
import android.os.Vibrator
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.preference.NeoTermPreference
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class TermSessionChangedCallback : TerminalSession.SessionChangedCallback {
    var termView: TerminalView? = null
    var extraKeysView: ExtraKeysView? = null
    var termTab: TermTab? = null

    var bellId: Int = 0
    var soundPool: SoundPool? = null

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
        if (termView == null) {
            return
        }

        if (NeoTermPreference.loadBoolean(R.string.key_general_bell, false)) {
            if (soundPool == null) {
                soundPool = SoundPool.Builder().setMaxStreams(1).build()
                bellId = soundPool!!.load(termView!!.context, R.raw.bell, 1)
            }
            soundPool?.play(bellId, 1f, 1f, 0, 0, 1f)
        }

        if (NeoTermPreference.loadBoolean(R.string.key_general_vibrate, false)) {
            val vibrator = termView!!.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
    }

    override fun onColorsChanged(session: TerminalSession?) {
    }
}