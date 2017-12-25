package io.neoterm.component.colorscheme

import io.neolang.visitor.ConfigVisitor
import io.neoterm.backend.TerminalColorScheme
import io.neoterm.backend.TerminalColors
import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.generator.ICodeGenerator
import io.neoterm.component.codegen.model.CodeGenObject
import io.neoterm.component.codegen.impl.NeoColorGenerator
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.component.helper.FileBasedComponentObject
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
import java.io.File

/**
 * @author kiva
 */
open class NeoColorScheme : CodeGenObject, FileBasedComponentObject {
    companion object {
        const val COLOR_PREFIX = "color"
        const val CONTEXT_COLOR_NAME = "colors"
        const val CONTEXT_META_NAME = "color-scheme"

        const val COLOR_META_NAME = "name"
        const val COLOR_META_VERSION = "version"
        const val COLOR_DEF_BACKGROUND = "background"
        const val COLOR_DEF_FOREGROUND = "foreground"
        const val COLOR_DEF_CURSOR = "cursor"

        val COLOR_META_PATH = arrayOf(CONTEXT_META_NAME)
        val COLOR_PATH = arrayOf(CONTEXT_META_NAME, CONTEXT_COLOR_NAME)

        const val COLOR_TYPE_BEGIN = -3
        const val COLOR_TYPE_END = 15

        const val COLOR_BACKGROUND = -3
        const val COLOR_FOREGROUND = -2
        const val COLOR_CURSOR = -1

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

    lateinit var colorName: String
    var colorVersion: String? = null

    var foregroundColor: String? = null
    var backgroundColor: String? = null
    var cursorColor: String? = null
    var color: MutableMap<Int, String> = mutableMapOf()

    fun setColor(type: Int, color: String) {
        if (type < 0) {
            when (type) {
                COLOR_BACKGROUND -> backgroundColor = color
                COLOR_FOREGROUND -> foregroundColor = color
                COLOR_CURSOR -> cursorColor = color
            }
            return
        }
        this.color[type] = color
    }

    fun getColor(type: Int): String? {
        validateColors()
        return when (type) {
            COLOR_BACKGROUND -> backgroundColor
            COLOR_FOREGROUND -> foregroundColor
            COLOR_CURSOR -> cursorColor
            else -> {
                if (type in (0 until color.size)) {
                    color[type]
                } else {
                    ""
                }
            }
        }
    }

    fun copy(): NeoColorScheme {
        val copy = NeoColorScheme()
        copy.colorName = colorName
        copy.backgroundColor = backgroundColor
        copy.foregroundColor = foregroundColor
        copy.cursorColor = cursorColor
        this.color.forEach { copy.color.put(it.key, it.value) }
        return copy
    }

    @Throws(RuntimeException::class)
    override fun onConfigLoaded(configVisitor: ConfigVisitor) {
        val colorName = getMetaByVisitor(configVisitor, COLOR_META_NAME)
                ?: throw RuntimeException("ColorScheme must have a name")

        this.colorName = colorName
        this.colorVersion = getMetaByVisitor(configVisitor, COLOR_META_VERSION)

        backgroundColor = getColorByVisitor(configVisitor, "background")
        foregroundColor = getColorByVisitor(configVisitor, "foreground")
        cursorColor = getColorByVisitor(configVisitor, "cursor")
        configVisitor.getContext(COLOR_PATH).getAttributes().forEach {
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
    }

    @Deprecated("Use ColorSchemeComponent#loadConfigure() instead")
    fun loadConfigure(file: File): Boolean {
        val loaderService = ComponentManager.getComponent<ConfigureComponent>()

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
        onConfigLoaded(visitor)
        return true
    }

    internal fun applyColorScheme(view: TerminalView?, extraKeysView: ExtraKeysView?) {
        validateColors()

        if (view != null) {
            val scheme = TerminalColorScheme()
            scheme.updateWith(foregroundColor, backgroundColor, cursorColor, color)
            val session = view.currentSession
            if (session != null && session.emulator != null) {
                session.emulator.setColorScheme(scheme)
            }
            view.setBackgroundColor(TerminalColors.parse(backgroundColor))
        }

        if (extraKeysView != null) {
            extraKeysView.setBackgroundColor(TerminalColors.parse(backgroundColor))
            extraKeysView.setTextColor(TerminalColors.parse(foregroundColor))
        }
    }

    override fun getCodeGenerator(parameter: CodeGenParameter): ICodeGenerator {
        return NeoColorGenerator(parameter)
    }

    private fun validateColors() {
        backgroundColor = backgroundColor ?: DefaultColorScheme.backgroundColor
        foregroundColor = foregroundColor ?: DefaultColorScheme.foregroundColor
        cursorColor = cursorColor ?: DefaultColorScheme.cursorColor
    }

    private fun getMetaByVisitor(visitor: ConfigVisitor, metaName: String): String? {
        return visitor.getStringValue(COLOR_META_PATH, metaName)
    }

    private fun getColorByVisitor(visitor: ConfigVisitor, colorName: String): String? {
        return visitor.getStringValue(COLOR_PATH, colorName)
    }
}