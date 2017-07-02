package io.neoterm.customize.font

import android.content.Context
import android.graphics.Typeface

/**
 * @author kiva
 */
object FontManager {
    private lateinit var DEFAULT_FONT: Typeface

    fun init(context: Context) {
        DEFAULT_FONT = Typeface.createFromAsset(context.assets, "font.ttf")
    }

    fun getDefaultFont(): Typeface {
        return DEFAULT_FONT
    }

    fun getCurrentFont(): Typeface {
        return DEFAULT_FONT
    }
}