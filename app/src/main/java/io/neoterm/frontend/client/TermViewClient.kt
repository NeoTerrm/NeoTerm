package io.neoterm.frontend.client

import android.content.Context
import android.media.AudioManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import io.neoterm.R
import io.neoterm.backend.KeyHandler
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.eks.ExtraKeysManager
import io.neoterm.frontend.service.ServiceManager
import io.neoterm.preference.NeoPreference
import io.neoterm.view.TerminalViewClient


/**
 * @author kiva
 */
class TermViewClient(val context: Context) : TerminalViewClient {
    private var mVirtualControlKeyDown: Boolean = false
    private var mVirtualFnKeyDown: Boolean = false
    private var lastTitle: String = ""

    var sessionFinished: Boolean = false

    var termData: TermDataHolder? = null

    override fun onScale(scale: Float): Float {
        if (scale < 0.9f || scale > 1.1f) {
            val increase = scale > 1f
            changeFontSize(increase)
            return 1.0f
        }
        return scale
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val termView = termData?.termView
        if (termView != null) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(termView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return NeoPreference.loadBoolean(R.string.key_generaL_backspace_map_to_esc, false)
    }

    override fun copyModeChanged(copyMode: Boolean) {
        // TODO
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        val termUI = termData?.termUI

        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (e?.action == KeyEvent.ACTION_DOWN && sessionFinished) {
                    termUI?.requireClose()
                    return true
                }
                return false
            }
            KeyEvent.KEYCODE_BACK -> {
                if (e?.action == KeyEvent.ACTION_DOWN) {
                    return termUI?.requireFinishAutoCompletion() ?: false
                }
                return false
            }
        }
        if (e != null && e.isCtrlPressed && e.isAltPressed) {
            // Get the unmodified code point:
            val unicodeChar = e.getUnicodeChar(0).toChar()

            if (unicodeChar == 'f'/* full screen */) {
                termUI?.requireToggleFullScreen()
            } else if (unicodeChar == 'v') {
                termUI?.requirePaste()
            } else if (unicodeChar == '+' || e.getUnicodeChar(KeyEvent.META_SHIFT_ON).toChar() == '+') {
                // We also check for the shifted char here since shift may be required to produce '+',
                // see https://github.com/termux/termux-api/issues/2
                changeFontSize(true)
            } else if (unicodeChar == '-') {
                changeFontSize(false)
            }
        }
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        return handleVirtualKeys(keyCode, e, false)
    }

    override fun readControlKey(): Boolean {
        val extraKeysView = termData?.extraKeysView
        return (extraKeysView != null && extraKeysView.readControlButton()) || mVirtualControlKeyDown
    }

    override fun readAltKey(): Boolean {
        val extraKeysView = termData?.extraKeysView
        return (extraKeysView != null && extraKeysView.readAltButton()) || mVirtualFnKeyDown
    }

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
        if (mVirtualFnKeyDown) {
            var resultingKeyCode: Int = -1
            var resultingCodePoint: Int = -1
            var altDown = false
            val lowerCase = Character.toLowerCase(codePoint)
            when (lowerCase.toChar()) {
            // Arrow keys.
                'w' -> resultingKeyCode = KeyEvent.KEYCODE_DPAD_UP
                'a' -> resultingKeyCode = KeyEvent.KEYCODE_DPAD_LEFT
                's' -> resultingKeyCode = KeyEvent.KEYCODE_DPAD_DOWN
                'd' -> resultingKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT

            // Page up and down.
                'p' -> resultingKeyCode = KeyEvent.KEYCODE_PAGE_UP
                'n' -> resultingKeyCode = KeyEvent.KEYCODE_PAGE_DOWN

            // Some special keys:
                't' -> resultingKeyCode = KeyEvent.KEYCODE_TAB
                'i' -> resultingKeyCode = KeyEvent.KEYCODE_INSERT
                'h' -> resultingCodePoint = '~'.toInt()

            // Special characters to input.
                'u' -> resultingCodePoint = '_'.toInt()
                'l' -> resultingCodePoint = '|'.toInt()

            // Function keys.
                '1', '2', '3', '4', '5', '6', '7', '8', '9' -> resultingKeyCode = codePoint - '1'.toInt() + KeyEvent.KEYCODE_F1
                '0' -> resultingKeyCode = KeyEvent.KEYCODE_F10

            // Other special keys.
                'e' -> resultingCodePoint = 27 /*Escape*/
                '.' -> resultingCodePoint = 28 /*^.*/

                'b' // alt+b, jumping backward in readline.
                    , 'f' // alf+f, jumping forward in readline.
                    , 'x' // alt+x, common in emacs.
                -> {
                    resultingCodePoint = lowerCase
                    altDown = true
                }

            // Volume control.
                'v' -> {
                    resultingCodePoint = -1
                    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audio.adjustSuggestedStreamVolume(AudioManager.ADJUST_SAME, AudioManager.USE_DEFAULT_STREAM_TYPE, AudioManager.FLAG_SHOW_UI)
                }
            }

            if (resultingKeyCode != -1) {
                if (session != null) {
                    val term = session.emulator
                    session.write(KeyHandler.getCode(resultingKeyCode, 0, term.isCursorKeysApplicationMode, term.isKeypadApplicationMode))
                }
            } else if (resultingCodePoint != -1) {
                session?.writeCodePoint(altDown, resultingCodePoint)
            }
            return true
        }
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

    fun updateSuggestions(title: String?, force: Boolean = false) {
        val extraKeysView = termData?.extraKeysView

        if (extraKeysView == null || title == null || title.isEmpty()) {
            return
        }

        if (lastTitle != title || force) {
            removeSuggestions()
            ServiceManager.getService<ExtraKeysManager>().showShortcutKeys(title, extraKeysView)
            extraKeysView?.updateButtons()
            lastTitle = title
        }
    }

    fun removeSuggestions() {
        val extraKeysView = termData?.extraKeysView
        extraKeysView?.clearUserKeys()
    }

    private fun changeFontSize(increase: Boolean) {
        val termView = termData?.termView
        if (termView != null) {
            val changedSize = (if (increase) 1 else -1) * 2
            val fontSize = termView.textSize + changedSize
            termView.textSize = fontSize
            NeoPreference.store(NeoPreference.KEY_FONT_SIZE, fontSize)
        }
    }
}