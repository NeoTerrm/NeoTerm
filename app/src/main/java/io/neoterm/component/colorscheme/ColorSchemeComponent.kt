package io.neoterm.component.colorscheme

import android.content.Context
import io.neolang.visitor.ConfigVisitor
import io.neoterm.App
import io.neoterm.R
import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.component.helper.ConfigFileBasedComponent
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
import io.neoterm.utils.AssetsUtils
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
class ColorSchemeComponent : ConfigFileBasedComponent<NeoColorScheme>() {
    companion object {
        fun colorFile(colorName: String): File {
            return File("${NeoTermPath.COLORS_PATH}/$colorName.nl")
        }
    }

    override val checkComponentFileWhenObtained = true

    private lateinit var DEFAULT_COLOR: NeoColorScheme
    private lateinit var colors: MutableMap<String, NeoColorScheme>

    override fun onCheckComponentFiles() {
        File(NeoTermPath.COLORS_PATH).mkdirs()
        colors = mutableMapOf()

        val defaultColorFile = colorFile(DefaultColorScheme.colorName)
        if (!defaultColorFile.exists()) {
            if (!extractDefaultColor(App.get())) {
                DEFAULT_COLOR = DefaultColorScheme
                colors[DEFAULT_COLOR.colorName] = DEFAULT_COLOR
                return
            }
        }

        if (!reloadColorSchemes()) {
            DEFAULT_COLOR = DefaultColorScheme
            colors[DEFAULT_COLOR.colorName] = DEFAULT_COLOR
        }
    }

    override fun onCreateComponentObject(configVisitor: ConfigVisitor): NeoColorScheme {
        return NeoColorScheme()
    }

    fun reloadColorSchemes(): Boolean {
        colors.clear()

        File(NeoTermPath.COLORS_PATH).listFiles { pathname ->
            pathname.name.endsWith(".color") || pathname.name.endsWith(".nl")
        }.forEach {
            val colorScheme = this.loadConfigure(it)
            if (colorScheme != null) {
                colors.put(colorScheme.colorName, colorScheme)
            }
        }

        if (colors.containsKey(DefaultColorScheme.colorName)) {
            DEFAULT_COLOR = colors[DefaultColorScheme.colorName]!!
            return true
        }
        return false
    }

    fun applyColorScheme(view: TerminalView?, extraKeysView: ExtraKeysView?, colorScheme: NeoColorScheme?) {
        colorScheme?.applyColorScheme(view, extraKeysView)
    }

    fun getCurrentColorScheme(): NeoColorScheme {
        return colors[getCurrentColorSchemeName()]!!
    }

    fun getCurrentColorSchemeName(): String {
        var currentColorName = NeoPreference.loadString(R.string.key_customization_color_scheme, DefaultColorScheme.colorName)
        if (!colors.containsKey(currentColorName)) {
            currentColorName = DefaultColorScheme.colorName
            NeoPreference.store(R.string.key_customization_color_scheme, DefaultColorScheme.colorName)
        }
        return currentColorName
    }

    fun getColorScheme(colorName: String): NeoColorScheme {
        return if (colors.containsKey(colorName)) colors[colorName]!! else getCurrentColorScheme()
    }

    fun getColorSchemeNames(): List<String> {
        val list = ArrayList<String>()
        list += colors.keys
        return list
    }

    fun setCurrentColorScheme(colorName: String) {
        NeoPreference.store(R.string.key_customization_color_scheme, colorName)
    }

    fun setCurrentColorScheme(color: NeoColorScheme) {
        setCurrentColorScheme(color.colorName)
    }

    private fun extractDefaultColor(context: Context): Boolean {
        try {
            AssetsUtils.extractAssetsDir(context, "colors", NeoTermPath.COLORS_PATH)
            return true
        } catch (e: Exception) {
            NLog.e("ColorScheme", "Failed to extract default colors: ${e.localizedMessage}")
            return false
        }
    }

    fun saveColorScheme(colorScheme: NeoColorScheme) {
        val colorFile = colorFile(colorScheme.colorName)
        if (colorFile.exists()) {
            throw RuntimeException("ColorScheme already ${colorScheme.colorName} exists!")
        }

        val component = ComponentManager.getComponent<CodeGenComponent>()
        val content = component.newGenerator(colorScheme).generateCode(colorScheme)

        if (!FileUtils.writeFile(colorFile, content.toByteArray())) {
            throw RuntimeException("Failed to save file ${colorFile.absolutePath}")
        }
    }
}