package io.neoterm.customize.font

import android.graphics.Typeface
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class NeoFont(val typeface: Typeface) {
    fun applyLocal(terminalView: TerminalView?) {
        terminalView?.setTypeface(typeface)
    }
}