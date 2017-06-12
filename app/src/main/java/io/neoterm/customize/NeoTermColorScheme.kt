package io.neoterm.customize

import io.neoterm.terminal.TerminalColors
import io.neoterm.terminal.TerminalSession

/**
 * @author kiva
 */
class NeoTermColorScheme {
    var foreground: String? = null
    var background: String? = null
    var cursor: String? = null
    var color: MutableMap<Int, String> = mutableMapOf()

    fun apply(session: TerminalSession?) {
        if (session == null) {
            return
        }

        val termSession = session
        TerminalColors.COLOR_SCHEME.updateWith(foreground, background, cursor, color)
        termSession.emulator.mColors.reset()
    }
}