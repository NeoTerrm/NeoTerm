package io.neoterm.customize.color

import io.neoterm.backend.TerminalEmulator

/**
 * @author kiva
 */
object ColorSchemeManager {
    fun applyColorScheme(emulator: TerminalEmulator?, colorScheme: NeoColorScheme?) {
        if (emulator != null && colorScheme != null) {
            colorScheme.applyLocal(emulator)
        }
    }

    fun applyGlobalColorScheme(colorScheme: NeoColorScheme?) {
        colorScheme?.applyGlobal()
    }
}