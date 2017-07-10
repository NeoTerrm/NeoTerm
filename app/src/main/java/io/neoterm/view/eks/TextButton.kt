package io.neoterm.view.eks

import android.view.View

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */

class TextButton @JvmOverloads constructor(text: String, withEnter: Boolean = false) : ExtraButton() {
    private var withEnter = false

    init {
        this.buttonText = text
        this.withEnter = withEnter
    }

    override fun onClick(view: View) {
        ExtraButton.sendKey(view, buttonText!!)
        if (withEnter) {
            ExtraButton.sendKey(view, "\n")
        }
    }
}
