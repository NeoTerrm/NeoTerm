package io.neoterm.view.tab

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import io.neoterm.backend.TerminalSession
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

/**
 * @author kiva
 */
class TermViewClient(val context: Context) : TerminalViewClient {
    private var mVirtualControlKeyDown: Boolean = false
    private var mVirtualFnKeyDown: Boolean = false
    var termView: TerminalView? = null
    var extraKeysView: ExtraKeysView? = null

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1f
            val changedSize = (if (increase) 1 else -1) * 2
            termView!!.textSize = termView!!.textSize + changedSize
            return 1.0f
        }
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(termView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun copyModeChanged(copyMode: Boolean) {
        // TODO
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        // TODO
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        return handleVirtualKeys(keyCode, e, false)
    }

    override fun readControlKey(): Boolean {
        return (extraKeysView != null && extraKeysView!!.readControlButton()) || mVirtualControlKeyDown
    }

    override fun readAltKey(): Boolean {
        return (extraKeysView != null && extraKeysView!!.readAltButton()) || mVirtualFnKeyDown
    }

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
        // TODO
        return false
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        // TODO
        return false
    }

    private fun handleVirtualKeys(keyCode: Int, event: KeyEvent?, down: Boolean): Boolean {
        if (event == null) {
            return false
        }
        val inputDevice = event.device
        if (inputDevice != null && inputDevice.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) {
            return false
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mVirtualControlKeyDown = down
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mVirtualFnKeyDown = down
            return true
        }
        return false
    }

}