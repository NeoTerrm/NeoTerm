package io.neoterm.component.color

import android.content.Context
import io.neoterm.App
import io.neoterm.R
import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.logging.NLog
import io.neoterm.utils.AssetsUtils
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.eks.ExtraKeysView
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
class ColorSchemeComponent : NeoComponent {
    companion object {
        fun colorFile(colorName: String): File {
            return File("${NeoTermPath.COLORS_PATH}/$colorName.nl")
        }
    }

    private lateinit var DEFAULT_COLOR: NeoColorScheme
    private lateinit var colors: MutableMap<String, NeoColorScheme>

    fun reloadColorSchemes(): Boolean {
        colors.clear()
        val colorDir = File(NeoTermPath.COLORS_PATH)
        for (file in colorDir.listFiles({ pathname ->
            pathname.name.endsWith(".color") || pathname.name.endsWith(".nl")
        })) {
            val color = NeoColorScheme()

            if (color.loadConfigure(file)) {
                colors.put(color.colorName, color)
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

    override fun onServiceObtained() {
        checkForFiles()
    }

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
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

    private fun checkForFiles() {
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

    fun saveColorScheme(colorScheme: NeoColorScheme) {
        val colorFile = colorFile(colorScheme.colorName)
        if (colorFile.exists()) {
            throw RuntimeException("ColorScheme ${colorScheme.colorName} exists!")
        }

        val component = ComponentManager.getComponent<CodeGenComponent>()
        val content = component.newGenerator(colorScheme).generateCode(colorScheme)

        if (!FileUtils.writeFile(colorFile, content.toByteArray())) {
            throw RuntimeException("Failed to save file ${colorFile.absolutePath}")
        }
    }
}