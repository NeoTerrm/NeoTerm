package io.neoterm.customize.color

import io.neoterm.backend.TerminalColorScheme
import io.neoterm.backend.TerminalColors
import io.neoterm.customize.config.ConfigureService
import io.neolang.visitor.ConfigVisitor
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.service.ServiceManager
import io.neoterm.view.TerminalView
import io.neoterm.view.eks.ExtraKeysView
import java.io.File

/**
 * @author kiva
 */
open class NeoColorScheme {
    companion object {
        private const val COLOR_PREFIX = "color"
        const val COLOR_CONTEXT_NAME = "colors"
        const val COLOR_META_CONTEXT_NAME = "color-scheme"

        const val COLOR_META_NAME = "name"
        const val COLOR_META_VERSION = "version"

        val COLOR_META_PATH = arrayOf(COLOR_META_CONTEXT_NAME)
        val COLOR_PATH = arrayOf(COLOR_META_CONTEXT_NAME, COLOR_CONTEXT_NAME)

//        const val COLOR_DIM_BLACK = 0
//        const val COLOR_DIM_RED = 1
//        const val COLOR_DIM_GREEN = 2
//        const val COLOR_DIM_YELLOW = 3
//        const val COLOR_DIM_BLUE = 4
//        const val COLOR_DIM_MAGENTA = 5
//        const val COLOR_DIM_CYAN = 6
//        const val COLOR_DIM_WHITE = 7
//
//        const val COLOR_BRIGHT_BLACK = 8
//        const val COLOR_BRIGHT_RED = 9
//        const val COLOR_BRIGHT_GREEN = 10
//        const val COLOR_BRIGHT_YELLOW = 11
//        const val COLOR_BRIGHT_BLUE = 12
//        const val COLOR_BRIGHT_MAGENTA = 13
//        const val COLOR_BRIGHT_CYAN = 14
//        const val COLOR_BRIGHT_WHITE = 15
    }

    lateinit var colorName: String
    var colorVersion: String? = null
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

    fun loadConfigure(file: File): Boolean {
        // TODO: Refactor with NeoExtraKey#loadConfigure
        val loaderService = ServiceManager.getService<ConfigureService>()

        val configure: NeoConfigureFile?
        try {
            configure = loaderService.newLoader(file).loadConfigure()
            if (configure == null) {
                throw RuntimeException("Parse configuration failed.")
            }
        } catch (e: Exception) {
            NLog.e("ExtraKey", "Failed to load extra key config: ${file.absolutePath}: ${e.localizedMessage}")
            return false
        }

        val visitor = configure.getVisitor()
        val colorName = getMetaByVisitor(visitor, COLOR_META_NAME)
        if (colorName == null) {
            NLog.e("ColorScheme", "Failed to load color config: ${file.absolutePath}: ColorScheme must have a name")
            return false
        }

        this.colorName = colorName
        this.colorVersion = getMetaByVisitor(visitor, COLOR_META_VERSION)

        backgroundColor = getColorByVisitor(visitor, "background")
        foregroundColor = getColorByVisitor(visitor, "foreground")
        cursorColor = getColorByVisitor(visitor, "cursor")
        visitor.getContext(COLOR_PATH).getAttributes().forEach {
            if (it.key.startsWith(COLOR_PREFIX)) {
                val colorIndex = try {
                    it.key.substringAfter(COLOR_PREFIX).toInt()
                } catch (e: Exception) {
                    -1
                }

                if (colorIndex == -1) {
                    NLog.w("ColorScheme", "Invalid color type: ${it.key}")
                } else {
                    setColor(colorIndex, it.value.asString())
                }
            }
        }

        validateColors()
        return true
    }

    private fun getMetaByVisitor(visitor: ConfigVisitor, metaName: String): String? {
        val value = visitor.getAttribute(COLOR_META_PATH, metaName)
        return if (value.isValid()) value.asString() else null
    }

    private fun getColorByVisitor(visitor: ConfigVisitor, colorName: String): String? {
        val value = visitor.getAttribute(COLOR_PATH, colorName)
        return if (value.isValid()) value.asString() else null
    }
}