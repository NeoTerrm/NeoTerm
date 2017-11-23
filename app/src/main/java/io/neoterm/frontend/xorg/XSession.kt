package io.neoterm.frontend.xorg

import android.content.Context
import io.neoterm.backend.TerminalSession

/**
 * @author kiva
 */

class XSession private constructor() {
    companion object {
        fun createSession(context: Context, parameter: XParameter) : XSession {
            return XSession()
        }
    }

    var mSessionName = "";
}
