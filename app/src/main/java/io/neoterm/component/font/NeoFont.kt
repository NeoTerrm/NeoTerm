package io.neoterm.component.font

import android.graphics.Typeface
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
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

    internal fun applyFont(terminalView: TerminalView?, extraKeysView: ExtraKeysView?) {
        val typeface = getTypeFace()
        terminalView?.setTypeface(typeface)
        extraKeysView?.setTypeface(typeface)
    }

    private fun getTypeFace(): Typeface? {
        if (typeface == null && fontFile == null) {
            return null
        }

        if (typeface == null) {
            typeface = Typeface.createFromFile(fontFile)
        }
        return typeface
    }
}