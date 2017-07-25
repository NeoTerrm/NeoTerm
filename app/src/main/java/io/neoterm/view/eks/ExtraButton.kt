package io.neoterm.view.eks

import android.view.KeyEvent
import android.view.View

import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */

abstract class ExtraButton : View.OnClickListener {

    var buttonText: String? = null

    abstract override fun onClick(view: View)

    companion object {
        val KEY_ESC = "Esc"
        val KEY_TAB = "Tab"
        val KEY_CTRL = "Ctrl"
        val  KEY_ALT = "Alt"
        val KEY_PAGE_UP = "PgUp"
        val KEY_PAGE_DOWN = "PgDn"
        val KEY_HOME = "Home"
        val KEY_END = "End"
        val KEY_FN = "Fn"
        val KEY_BACK_SLASH = "\\"
        val KEY_ARROW_UP = "▲"
        val KEY_ARROW_DOWN = "▼"
        val KEY_ARROW_LEFT = "◀"
        val KEY_ARROW_RIGHT = "▶"
        val KEY_TOGGLE_FULL_SCREEN = "Fs"
        val KEY_TOGGLE_IME = "Im"

        fun sendKey(view: View, keyName: String) {
            var keyCode = 0
            var chars: String? = null
            when (keyName) {
                KEY_ESC -> keyCode = KeyEvent.KEYCODE_ESCAPE
                KEY_TAB -> keyCode = KeyEvent.KEYCODE_TAB
                KEY_ARROW_UP -> keyCode = KeyEvent.KEYCODE_DPAD_UP
                KEY_ARROW_LEFT -> keyCode = KeyEvent.KEYCODE_DPAD_LEFT
                KEY_ARROW_RIGHT -> keyCode = KeyEvent.KEYCODE_DPAD_RIGHT
                KEY_ARROW_DOWN -> keyCode = KeyEvent.KEYCODE_DPAD_DOWN
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
            } else {
                val terminalView = view.findViewById(R.id.terminal_view) as TerminalView
                val session = terminalView.currentSession
                session?.write(chars)
            }
        }
    }
}