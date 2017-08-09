package io.neoterm.customize.color

import android.content.Context
import io.neoterm.App
import io.neoterm.R
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.frontend.service.NeoService
import io.neoterm.utils.AssetsUtils
import io.neoterm.utils.FileUtils
import io.neoterm.view.TerminalView
import io.neoterm.view.eks.ExtraKeysView
import java.io.File

/**
 * @author kiva
 */
class ColorSchemeService : NeoService {
    override fun onServiceObtained() {
        checkForFiles()
    }

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    private lateinit var DEFAULT_COLOR: NeoColorScheme
    private lateinit var colors: MutableMap<String, NeoColorScheme>

    private fun extractDefaultColor(context: Context): Boolean {
        try {
            AssetsUtils.extractAssetsDir(context, "colors", NeoTermPath.COLORS_PATH)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun refreshColorList(): Boolean {
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
        if (view != null && colorScheme != null) {
            colorScheme.applyColors(view, extraKeysView)
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

        if (!refreshColorList()) {
            DEFAULT_COLOR = DefaultColorScheme
            colors[DEFAULT_COLOR.colorName] = DEFAULT_COLOR
        }
    }

    fun getCurrentColorScheme(): NeoColorScheme {
        return colors[getCurrentColorName()]!!
    }

    fun getCurrentColorName(): String {
        var currentColorName = NeoPreference.loadString(R.string.key_customization_color_scheme, DefaultColorScheme.colorName)
        if (!colors.containsKey(currentColorName)) {
            currentColorName = DefaultColorScheme.colorName
            NeoPreference.store(R.string.key_customization_color_scheme, DefaultColorScheme.colorName)
        }
        return currentColorName
    }

    fun getColor(colorName: String): NeoColorScheme {
        return if (colors.containsKey(colorName)) colors[colorName]!! else getCurrentColorScheme()
    }

    fun getColorNames(): List<String> {
        val list = ArrayList<String>()
        list += colors.keys
        return list
    }

    fun setCurrentColor(colorName: String) {
        NeoPreference.store(R.string.key_customization_color_scheme, colorName)
    }

    companion object {
        fun colorFile(colorName: String): File {
            return File("${NeoTermPath.COLORS_PATH}/$colorName.nl")
        }
    }
}