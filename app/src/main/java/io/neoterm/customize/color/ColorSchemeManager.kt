package io.neoterm.customize.color

import android.content.Context
import io.neoterm.backend.TerminalEmulator
import io.neoterm.customize.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
object ColorSchemeManager {
    fun init(context: Context) {
        File(NeoTermPath.COLORS_PATH).mkdirs()
    }

    fun applyColorScheme(emulator: TerminalEmulator?, colorScheme: NeoColorScheme?) {
        if (emulator != null && colorScheme != null) {
            colorScheme.applyLocal(emulator)
        }
    }

    fun applyGlobalColorScheme(colorScheme: NeoColorScheme?) {
        colorScheme?.applyGlobal()
    }
}