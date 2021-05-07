package io.neoterm.ui.term.tab

import android.content.res.Configuration
import io.neoterm.frontend.session.xorg.XSession

/**
 * @author kiva
 */
class XSessionTab(title: CharSequence) : NeoTab(title) {
  var session: XSession? = null
  val sessionData
    get() = session?.mSessionData

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (!hasFocus) {
      onPause()
    } else {
      onResume()
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    session?.updateScreenOrientation()
  }

  override fun onPause() {
    session?.onPause()
    super.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    session?.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    session?.onResume()
  }
}
