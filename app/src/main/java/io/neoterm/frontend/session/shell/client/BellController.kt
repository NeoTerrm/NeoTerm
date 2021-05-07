package io.neoterm.frontend.session.shell.client

import android.content.Context
import android.media.SoundPool
import android.os.Vibrator
import io.neoterm.R
import io.neoterm.frontend.session.shell.ShellTermSession

/**
 * @author kiva
 */
class BellController constructor() {
  companion object {
    private val BELL_DELAY_MS = 100
  }

  private var bellId: Int = 0
  private var soundPool: SoundPool? = null
  private var lastBellTime = 0L

  fun bellOrVibrate(context: Context, session: ShellTermSession) {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastBellTime < BELL_DELAY_MS) {
      return
    }
    lastBellTime = currentTime

    if (session.shellProfile.enableBell) {
      if (soundPool == null) {
        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        bellId = soundPool!!.load(context, R.raw.bell, 1)
      }
      soundPool?.play(bellId, 1f, 1f, 0, 0, 1f)
    }

    if (session.shellProfile.enableVibrate) {
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(100)
    }
  }
}