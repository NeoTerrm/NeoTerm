package io.neoterm.frontend.terminal.extrakey.button

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import io.neoterm.R
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.extrakey.combine.CombinedSequence

/**
 * @author kiva
 */

abstract class IExtraButton : View.OnClickListener {

  var buttonKeys: CombinedSequence? = null
  var displayText: String? = null

  override fun toString(): String {
    return "${this.javaClass.simpleName} { display: $displayText, code: ${buttonKeys?.keys} }"
  }

  abstract override fun onClick(view: View)

  abstract fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button

  companion object {
    val KEY_ESC = "Esc"
    val KEY_TAB = "Tab"
    val KEY_CTRL = "Ctrl"
    val KEY_ALT = "Alt"
    val KEY_PAGE_UP = "PgUp"
    val KEY_PAGE_DOWN = "PgDn"
    val KEY_HOME = "Home"
    val KEY_END = "End"
    val KEY_ARROW_UP_TEXT = "Up"
    val KEY_ARROW_DOWN_TEXT = "Down"
    val KEY_ARROW_LEFT_TEXT = "Left"
    val KEY_ARROW_RIGHT_TEXT = "Right"
    val KEY_SHOW_ALL_BUTTONS = "···"
    val KEY_TOGGLE_IME = "Im"

    val KEY_ARROW_UP = "▲"
    val KEY_ARROW_DOWN = "▼"
    val KEY_ARROW_LEFT = "◀"
    val KEY_ARROW_RIGHT = "▶"

    var NORMAL_TEXT_COLOR = 0xFFFFFFFF.toInt()
    var SELECTED_TEXT_COLOR = 0xFF80DEEA.toInt()

    fun sendKey(view: View, keyName: String) {
      var keyCode = 0
      var chars = ""
      when (keyName) {
        KEY_ESC -> keyCode = KeyEvent.KEYCODE_ESCAPE
        KEY_TAB -> keyCode = KeyEvent.KEYCODE_TAB
        KEY_ARROW_UP -> keyCode = KeyEvent.KEYCODE_DPAD_UP
        KEY_ARROW_LEFT -> keyCode = KeyEvent.KEYCODE_DPAD_LEFT
        KEY_ARROW_RIGHT -> keyCode = KeyEvent.KEYCODE_DPAD_RIGHT
        KEY_ARROW_DOWN -> keyCode = KeyEvent.KEYCODE_DPAD_DOWN
        KEY_ARROW_UP_TEXT -> keyCode = KeyEvent.KEYCODE_DPAD_UP
        KEY_ARROW_LEFT_TEXT -> keyCode = KeyEvent.KEYCODE_DPAD_LEFT
        KEY_ARROW_RIGHT_TEXT -> keyCode = KeyEvent.KEYCODE_DPAD_RIGHT
        KEY_ARROW_DOWN_TEXT -> keyCode = KeyEvent.KEYCODE_DPAD_DOWN
        KEY_PAGE_UP -> keyCode = KeyEvent.KEYCODE_PAGE_UP
        KEY_PAGE_DOWN -> keyCode = KeyEvent.KEYCODE_PAGE_DOWN
        KEY_HOME -> keyCode = KeyEvent.KEYCODE_MOVE_HOME
        KEY_END -> keyCode = KeyEvent.KEYCODE_MOVE_END
        "―" -> chars = "-"
        else -> chars = keyName
      }

      if (keyCode > 0) {
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
      } else if (chars.isNotEmpty()) {
        val terminalView = view.findViewById<TerminalView>(R.id.terminal_view)
        val session = terminalView.currentSession
        session?.write(chars)
      }
    }
  }
}