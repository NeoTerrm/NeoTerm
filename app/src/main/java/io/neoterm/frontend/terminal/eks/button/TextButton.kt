package io.neoterm.frontend.terminal.eks.button

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button

/**
 * @author kiva
 */

open class TextButton @JvmOverloads constructor(text: String, withEnter: Boolean = false) : IExtraButton() {
    var withEnter = false

    init {
        this.buttonText = text
        this.displayText = text
        this.withEnter = withEnter
    }

    override fun onClick(view: View) {
        sendKey(view, buttonText!!)
        if (withEnter) {
            sendKey(view, "\n")
        }
    }

    override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
        return Button(context, attrs, defStyleAttr)
    }
}
