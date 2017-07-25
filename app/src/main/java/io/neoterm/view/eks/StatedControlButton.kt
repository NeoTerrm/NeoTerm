package io.neoterm.view.eks

import android.view.View
import android.widget.ToggleButton

/**
 * @author kiva
 */

open class StatedControlButton @JvmOverloads constructor(text: String, var initState: Boolean = false) : ControlButton(text) {
    var toggleButton: ToggleButton? = null

    override fun onClick(view: View) {
        setStatus(toggleButton!!.isChecked)
    }

    fun setStatus(status: Boolean) {
        toggleButton!!.isChecked = status
        toggleButton!!.setTextColor(if (status) ExtraKeysView.SELECTED_TEXT_COLOR else ExtraKeysView.NORMAL_TEXT_COLOR)
    }

    fun readState(): Boolean {
        if (toggleButton!!.isPressed) return true
        val result = toggleButton!!.isChecked
        if (result) {
            toggleButton!!.isChecked = false
            toggleButton!!.setTextColor(ExtraKeysView.NORMAL_TEXT_COLOR)
        }
        return result
    }
}
