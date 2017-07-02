package io.neoterm.customize.color

import io.neoterm.backend.TerminalColorScheme
import io.neoterm.backend.TerminalColors
import io.neoterm.backend.TerminalEmulator

/**
 * @author kiva
 */
open class NeoColorScheme {
    var foreground: String? = null
    var background: String? = null
    var cursor: String? = null
    var color: MutableMap<Int, String> = mutableMapOf()

    fun applyGlobal() {
        TerminalColors.COLOR_SCHEME.updateWith(foreground, background, cursor, color)
    }

    fun applyLocal(emulator: TerminalEmulator) {
        val scheme = TerminalColorScheme()
        scheme.updateWith(foreground, background, cursor, color)
        emulator.mColors.reset(scheme)
    }
}