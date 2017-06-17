package io.neoterm.customize.font

import android.content.Context
import android.graphics.Typeface

/**
 * @author kiva
 */
object FontManager {
    private var DEFAULT_FONT: Typeface? = null

    fun init(context: Context) {
        DEFAULT_FONT = Typeface.createFromAsset(context.assets, "font.ttf")
    }

    fun getDefaultFont(): Typeface {
        return DEFAULT_FONT!!
    }
}