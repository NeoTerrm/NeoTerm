package io.neoterm.frontend.completion.listener

/**
 * @author Kiva
 * *
 * @version 1.0
 */
interface OnAutoCompleteListener {
    fun onAutoComplete(newText: String?)

    fun onKeyCode(keyCode: Int, keyMod: Int)

    fun onCleanUp()
}
