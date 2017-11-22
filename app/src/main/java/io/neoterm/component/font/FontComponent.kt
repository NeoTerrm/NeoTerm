package com.termux.component.font

import com.termux.content.Context
import android.graphics.Typeface
import android.view.View
import com.termux.App
import com.termux.R
import com.termux.frontend.preference.NeoPreference
import com.termux.frontend.preference.NeoTermPath
import com.termux.frontend.component.NeoComponent
import com.termux.frontend.terminal.TerminalView
import com.termux.frontend.terminal.eks.ExtraKeysView
import com.termux.utils.AssetsUtils
import java.io.File

/**
 * @author Sam
 */
class FontComponent : NeoComponent {
    private val DEFAULT_FONT_NAME = "SourceCodePro"

    private lateinit var DEFAULT_FONT: NeoFont
    private lateinit var fonts: MutableMap<String, NeoFont>

    fun applyFont(terminalView: TerminalView?, extraKeysView: ExtraKeysView?, font: NeoFont?) {
        font?.applyFont(terminalView, extraKeysView)
    }

    fun getCurrentFont(): NeoFont {
        return fonts[getCurrentFontName()]!!
    }

    fun setCurrentFont(fontName: String) {
        NeoPreference.store(R.string.key_customization_font, fontName)
    }

    fun getCurrentFontName(): String {
        var currentFontName = NeoPreference.loadString(R.string.key_customization_font, DEFAULT_FONT_NAME)
        if (!fonts.containsKey(currentFontName)) {
            currentFontName = DEFAULT_FONT_NAME
            NeoPreference.store(R.string.key_customization_font, DEFAULT_FONT_NAME)
        }
        return currentFontName
    }

    fun getFont(fontName: String): NeoFont {
        return if (fonts.containsKey(fontName)) fonts[fontName]!! else getCurrentFont()
    }

    fun getFontNames(): List<String> {
        val list = ArrayList<String>()
        list += fonts.keys
        return list
    }

    fun reloadFonts(): Boolean {
        fonts.clear()
        fonts.put("Android Monospace", NeoFont(Typeface.MONOSPACE))
        fonts.put("Android Sans Serif", NeoFont(Typeface.SANS_SERIF))
        fonts.put("Android Serif", NeoFont(Typeface.SERIF))
        val fontDir = File(NeoTermPath.FONT_PATH)
        for (file in fontDir.listFiles({ pathname -> pathname.name.endsWith(".ttf") })) {
            val fontName = fontName(file)
            val font = NeoFont(file)
            fonts.put(fontName, font)
        }
        if (fonts.containsKey(DEFAULT_FONT_NAME)) {
            DEFAULT_FONT = fonts[DEFAULT_FONT_NAME]!!
            return true
        }
        return false
    }

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }

    private fun loadDefaultFontFromAsset(context: Context): NeoFont {
        return NeoFont(Typeface.createFromAsset(context.assets, "fonts/$DEFAULT_FONT_NAME.ttf"))
    }

    private fun extractDefaultFont(context: Context): Boolean {
        try {
            AssetsUtils.extractAssetsDir(context, "fonts", NeoTermPath.FONT_PATH)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun fontFile(fontName: String): File {
        return File("${NeoTermPath.FONT_PATH}/$fontName.ttf")
    }

    private fun fontName(fontFile: File): String {
        return fontFile.nameWithoutExtension
    }

    private fun checkForFiles() {
        File(NeoTermPath.FONT_PATH).mkdirs()
        fonts = mutableMapOf()

        val context = App.get()
        val defaultFontFile = fontFile(DEFAULT_FONT_NAME)
        if (!defaultFontFile.exists()) {
            if (!extractDefaultFont(context)) {
                DEFAULT_FONT = loadDefaultFontFromAsset(context)
                fonts.put(DEFAULT_FONT_NAME, DEFAULT_FONT)
                return
            }
        }

        if (!reloadFonts()) {
            DEFAULT_FONT = loadDefaultFontFromAsset(context)
            fonts.put(DEFAULT_FONT_NAME, DEFAULT_FONT)
        }
    }
}
