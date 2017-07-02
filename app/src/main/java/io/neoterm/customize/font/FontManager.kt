package io.neoterm.customize.font

import android.content.Context
import android.graphics.Typeface

/**
 * @author kiva
 */
object FontManager {
    private lateinit var DEFAULT_FONT: NeoFont
    private lateinit var fonts: MutableList<String>

    fun init(context: Context) {
        fonts = mutableListOf()
        DEFAULT_FONT = NeoFont(Typeface.createFromAsset(context.assets, "font.ttf"))
    }

    fun getDefaultFont(): NeoFont {
        return DEFAULT_FONT
    }

    fun getCurrentFont(): NeoFont {
        return getDefaultFont()
    }
}