package io.neoterm.view.eks

import android.view.View

/**
 * @author kiva
 */

open class ControlButton(text: String) : ExtraButton() {
    init {
        buttonText = text
    }

    override fun onClick(view: View) {
        ExtraButton.Companion.sendKey(view, buttonText!!)
    }
}
