package io.neoterm.customize.color

import android.content.Context
import io.neoterm.R
import io.neoterm.preference.NeoPreference
import io.neoterm.preference.NeoTermPath
import io.neoterm.utils.FileUtils
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import java.io.File
import java.io.FileOutputStream

/**
 * @author kiva
 */
object ColorSchemeManager {
    private const val DEFAULT_COLOR_NAME = "Default"

    private lateinit var DEFAULT_COLOR: NeoColorScheme
    private lateinit var colors: MutableMap<String, NeoColorScheme>

    fun init(context: Context) {
        File(NeoTermPath.COLORS_PATH).mkdirs()
        colors = mutableMapOf()

        val defaultColorFile = colorFile(DEFAULT_COLOR_NAME)
        if (!defaultColorFile.exists()) {
            if (extractDefaultColor(context, defaultColorFile)) {
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

    private fun extractDefaultColor(context: Context, defaultColorFile: File): Boolean {
        try {
            val prop = DefaultColorScheme.createConfig()
            FileOutputStream(defaultColorFile).use {
                prop.store(it, "Created by NeoColorSchemeManager")
            }

            val asset = context.assets
            for (i in asset.list("colors")) {
                val targetFile = File(NeoTermPath.COLORS_PATH, i)
                if (!targetFile.exists()) {
                    asset.open("colors/$i").use {
                        FileUtils.writeFile(targetFile, it)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun refreshColorList(): Boolean {
        colors.clear()
        val colorDir = File(NeoTermPath.COLORS_PATH)
        for (file in colorDir.listFiles({ pathname -> pathname.name.endsWith(".color") })) {
            val colorName = colorName(file)
            val color = NeoColorScheme(colorName)

            color.parseConfig(file)
            colors.put(colorName, color)
        }
        if (colors.containsKey(DEFAULT_COLOR_NAME)) {
            DEFAULT_COLOR = colors[DEFAULT_COLOR_NAME]!!
            return true
        }
        return false
    }

    fun applyColorScheme(view: TerminalView?, extraKeysView: ExtraKeysView?, colorScheme: NeoColorScheme?) {
        if (view != null && colorScheme != null) {
            colorScheme.applyColors(view, extraKeysView)
        }
    }

    private fun colorFile(colorName: String): File {
        return File("${NeoTermPath.COLORS_PATH}/$colorName.color")
    }

    private fun colorName(colorFile: File): String {
        return colorFile.nameWithoutExtension
    }

    fun getCurrentColorScheme(): NeoColorScheme {
        return colors[getCurrentColorName()]!!
    }

    fun getCurrentColorName(): String {
        var currentColorName = NeoPreference.loadString(R.string.key_customization_color_scheme, DEFAULT_COLOR_NAME)
        if (!colors.containsKey(currentColorName)) {
            currentColorName = DEFAULT_COLOR_NAME
            NeoPreference.store(R.string.key_customization_color_scheme, DEFAULT_COLOR_NAME)
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
}