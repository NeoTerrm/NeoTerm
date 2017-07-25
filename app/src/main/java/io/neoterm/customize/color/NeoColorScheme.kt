package io.neoterm.customize.color

import io.neoterm.backend.TerminalColorScheme
import io.neoterm.backend.TerminalColors
import io.neoterm.view.eks.ExtraKeysView
import io.neoterm.view.TerminalView
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * @author kiva
 */
open class NeoColorScheme(val colorName: String) {
    companion object {
        private const val COLOR_PREFIX = "color"

        const val COLOR_DIM_BLACK = 0
        const val COLOR_DIM_RED = 1
        const val COLOR_DIM_GREEN = 2
        const val COLOR_DIM_YELLOW = 3
        const val COLOR_DIM_BLUE = 4
        const val COLOR_DIM_MAGENTA = 5
        const val COLOR_DIM_CYAN = 6
        const val COLOR_DIM_WHITE = 7

        const val COLOR_BRIGHT_BLACK = 8
        const val COLOR_BRIGHT_RED = 9
        const val COLOR_BRIGHT_GREEN = 10
        const val COLOR_BRIGHT_YELLOW = 11
        const val COLOR_BRIGHT_BLUE = 12
        const val COLOR_BRIGHT_MAGENTA = 13
        const val COLOR_BRIGHT_CYAN = 14
        const val COLOR_BRIGHT_WHITE = 15
    }

    var foregroundColor: String? = null
    var backgroundColor: String? = null
    var cursorColor: String? = null
    var color: MutableMap<Int, String> = mutableMapOf()

    fun setColor(type: Int, color: String) {
        this.color[type] = color
    }

    fun applyColors(view: TerminalView, extraKeysView: ExtraKeysView?) {
        validateColors()
        val scheme = TerminalColorScheme()
        scheme.updateWith(foregroundColor, backgroundColor, cursorColor, color)
        val session = view.currentSession
        if (session != null && session.emulator != null) {
            session.emulator.setColorScheme(scheme)
        }

        view.setBackgroundColor(TerminalColors.parse(backgroundColor))
        extraKeysView?.setBackgroundColor(TerminalColors.parse(backgroundColor))
        extraKeysView?.setTextColor(TerminalColors.parse(foregroundColor))
    }

    private fun validateColors() {
        backgroundColor = backgroundColor ?: DefaultColorScheme.backgroundColor
        foregroundColor = foregroundColor ?: DefaultColorScheme.foregroundColor
        cursorColor = cursorColor ?: DefaultColorScheme.cursorColor
    }

    fun createConfig(): Properties {
        // TODO: 设计新的配色方案语法，这个只是临时用一下
        validateColors()
        val prop = Properties()
        prop.put("foreground", foregroundColor)
        prop.put("background", backgroundColor)
        prop.put("cursor", cursorColor)
        for (i in color.keys) {
            prop.put(COLOR_PREFIX + i, color[i])
        }
        return prop
    }

    fun parseConfig(file: File): Boolean {
        try {
            return FileInputStream(file).use {
                val prop = Properties()
                prop.load(it)
                prop.all {
                    when (it.key) {
                        "foreground" -> foregroundColor = it.value as String
                        "background" -> backgroundColor = it.value as String
                        "cursor" -> cursorColor = it.value as String
                        (it.key as String).startsWith(COLOR_PREFIX) -> {
                            val colorType = (it.key as String).substringAfter(COLOR_PREFIX)
                            setColor(colorType.toInt(), it.value as String)
                        }
                    }
                    true
                }
                true
            }
        } catch (e: Exception) {
            return false
        }
    }
}