package io.neoterm.component.font

import android.graphics.Typeface
import java.io.File

/**
 * @author kiva
 */
class NeoFont {
    private var fontFile: File? = null
    private var typeface: Typeface? = null

    constructor(fontFile: File) {
        this.fontFile = fontFile
    }

    constructor(typeface: Typeface) {
        this.typeface = typeface
    }

    fun getTypeFace(): Typeface? {
        if (typeface == null && fontFile == null) {
            return null
        }

        if (typeface == null) {
            typeface = Typeface.createFromFile(fontFile)
        }
        return typeface
    }
}