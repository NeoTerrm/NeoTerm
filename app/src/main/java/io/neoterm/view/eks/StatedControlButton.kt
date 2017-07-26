package io.neoterm.view.eks

import android.view.View
import android.widget.ToggleButton

/**
 * @author kiva
 */

open class StatedControlButton @JvmOverloads constructor(text: String, var initState: Boolean = false) : ControlButton(text) {
    var toggleButton: ToggleButton? = null

    override fun onClick(view: View) {
        setStatus(toggleButton?.isChecked)
    }

    fun setStatus(status: Boolean?) {
        val button = toggleButton
        if (button != null && status != null) {
            button.isChecked = status
            button.setTextColor(
                    if (status) ExtraButton.SELECTED_TEXT_COLOR
                    else ExtraButton.NORMAL_TEXT_COLOR
            )
        }
    }

    fun readState(): Boolean {
        val button = toggleButton ?: return false

        if (button.isPressed) return true
        val result = button.isChecked
        if (result) {
            button.isChecked = false
            button.setTextColor(ExtraButton.NORMAL_TEXT_COLOR)
        }
        return result
    }
}
