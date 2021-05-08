package io.neoterm.frontend.session.view.extrakey

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatButton
import io.neoterm.R
import io.neoterm.frontend.session.view.TerminalView

/**
 * @author kiva
 */

open class ControlButton(text: String) : TextButton(text, false)

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
    const val KEY_ESC = "Esc"
    const val KEY_TAB = "Tab"
    const val KEY_CTRL = "Ctrl"
    const val KEY_ALT = "Alt"
    const val KEY_PAGE_UP = "PgUp"
    const val KEY_PAGE_DOWN = "PgDn"
    const val KEY_HOME = "Home"
    const val KEY_END = "End"
    const val KEY_ARROW_UP_TEXT = "Up"
    const val KEY_ARROW_DOWN_TEXT = "Down"
    const val KEY_ARROW_LEFT_TEXT = "Left"
    const val KEY_ARROW_RIGHT_TEXT = "Right"
    const val KEY_SHOW_ALL_BUTTONS = "···"
    const val KEY_TOGGLE_IME = "Im"

    const val KEY_ARROW_UP = "▲"
    const val KEY_ARROW_DOWN = "▼"
    const val KEY_ARROW_LEFT = "◀"
    const val KEY_ARROW_RIGHT = "▶"

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

/**
 * @author kiva
 */
open class RepeatableButton(buttonText: String) : ControlButton(buttonText) {

  override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
    return RepeatableButtonWidget(context, attrs, defStyleAttr)
  }

  private class RepeatableButtonWidget(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatButton(context!!, attrs, defStyleAttr) {

    /**
     * Milliseconds how long we trigger an action
     * when long pressing
     */
    private val LONG_CLICK_ACTION_INTERVAL = 100L

    private var isMotionEventUp = true

    var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
      override fun handleMessage(msg: android.os.Message) {
        if (!isMotionEventUp && isEnabled) {
          performClick()
          this.sendEmptyMessageDelayed(0, LONG_CLICK_ACTION_INTERVAL)
        }
      }
    }

    init {
      this.setOnLongClickListener {
        isMotionEventUp = false
        mHandler.sendEmptyMessage(0)
        false
      }
      this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
          isMotionEventUp = true
        }
        false
      }
    }

    override fun performClick(): Boolean {
      return super.performClick()
    }
  }
}

/**
 * @author kiva
 */

open class StatedControlButton @JvmOverloads constructor(text: String, var initState: Boolean = false) :
  ControlButton(text) {
  var toggleButton: ToggleButton? = null

  override fun onClick(view: View) {
    setStatus(toggleButton?.isChecked)
  }

  override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
    val outerButton = ToggleButton(context, null, android.R.attr.buttonBarButtonStyle)

    outerButton.isClickable = true
    if (initState) {
      outerButton.isChecked = true
      outerButton.setTextColor(SELECTED_TEXT_COLOR)
    }

    this.toggleButton = outerButton
    return outerButton
  }

  private fun setStatus(status: Boolean?) {
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

/**
 * @author kiva
 */

open class TextButton constructor(text: String, val withEnter: Boolean = false) : IExtraButton() {
  init {
    this.buttonKeys = CombinedSequence.solveString(text)
    this.displayText = text
  }

  override fun onClick(view: View) {
    buttonKeys!!.keys.forEach {
      sendKey(view, it)
    }
    if (withEnter) {
      sendKey(view, "\n")
    }
  }

  override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
    return Button(context, attrs, defStyleAttr)
  }
}

/**
 * @author kiva
 */
class ArrowButton(arrowText: String) : RepeatableButton(arrowText)
