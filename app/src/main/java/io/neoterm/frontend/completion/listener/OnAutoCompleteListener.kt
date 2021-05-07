package io.neoterm.frontend.completion.listener

/**
 * @author Kiva
 * *
 * @version 1.0
 */
interface OnAutoCompleteListener {
  fun onCompletionRequired(newText: String?)

  fun onKeyCode(keyCode: Int, keyMod: Int)

  fun onCleanUp()

  fun onFinishCompletion(): Boolean
}
