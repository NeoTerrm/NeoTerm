package io.neoterm.frontend.terminal.eks.button

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ToggleButton

/**
 * @author kiva
 */

open class StatedControlButton @JvmOverloads constructor(text: String, var initState: Boolean = false) : ControlButton(text) {
    var toggleButton: ToggleButton? = null

    override fun onClick(view: View) {
        setStatus(toggleButton?.isChecked)
    }

    override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
        val outerButton = ToggleButton(context, null, android.R.attr.buttonBarButtonStyle)

        outerButton.isClickable = true
        if (initState) {
            outerButton.isChecked = true
            outerButton.setTextColor(IExtraButton.SELECTED_TEXT_COLOR)
        }

        this.toggleButton = outerButton
        return outerButton
    }

    fun setStatus(status: Boolean?) {
        val button = toggleButton
        if (button != null && status != null) {
            button.isChecked = status
            button.setTextColor(
                    if (status) SELECTED_TEXT_COLOR
                    else NORMAL_TEXT_COLOR
            )
        }
    }

    fun readState(): Boolean {
        val button = toggleButton ?: return false

        if (button.isPressed) return true
        val result = button.isChecked
        if (result) {
            button.isChecked = false
            button.setTextColor(NORMAL_TEXT_COLOR)
        }
        return result
    }
}
