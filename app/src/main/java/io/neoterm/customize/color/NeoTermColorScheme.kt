package io.neoterm.customize.color

import io.neoterm.backend.TerminalColors

/**
 * @author kiva
 */
open class NeoTermColorScheme {
    var foreground: String? = null
    var background: String? = null
    var cursor: String? = null
    var color: MutableMap<Int, String> = mutableMapOf()

    fun apply() {
        TerminalColors.COLOR_SCHEME.updateWith(foreground, background, cursor, color)
    }
}