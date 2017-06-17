package io.neoterm.customize.font

import android.graphics.Typeface
import io.neoterm.NeoApp

/**
 * @author kiva
 */
object FontManager {
    private var DEFAULT_FONT: Typeface? = null

    fun getDefaultFont(): Typeface {
        if (DEFAULT_FONT == null) {
            DEFAULT_FONT = Typeface.createFromAsset(NeoApp.get().assets, "font.ttf")
        }
        return DEFAULT_FONT!!
    }
}