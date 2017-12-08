package io.neoterm.frontend.session.shell.client

import android.content.Context
import android.media.AudioManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.neoterm.R
import io.neoterm.backend.KeyHandler
import io.neoterm.backend.TerminalSession
import io.neoterm.component.eks.ExtraKeysComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.preference.DefaultPreference
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.frontend.session.shell.ShellTermSession
import io.neoterm.frontend.terminal.TerminalViewClient


/**
 * @author kiva
 */
class TermViewClient(val context: Context) : TerminalViewClient {
    private var mVirtualControlKeyDown: Boolean = false
    private var mVirtualFnKeyDown: Boolean = false
    private var lastTitle: String = ""

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
        val termView = termData?.termView ?: return
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(termView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        val shellSession = termData?.termSession as ShellTermSession? ?: return false
        return shellSession.shellProfile.enableBackButtonBeMappedToEscape
    }

    override fun copyModeChanged(copyMode: Boolean) {
        // TODO
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        if (handleVirtualKeys(keyCode, e, true)) {
            return true
        }

        val termUI = termData?.termUI

        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (e?.action == KeyEvent.ACTION_DOWN && session?.isRunning == false) {
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

        // TODO 自定义快捷键
        if (e != null && e.isCtrlPressed && e.isShiftPressed) {
            // Get the unmodified code point:
            val unicodeChar = e.getUnicodeChar(0).toChar()

            when (unicodeChar) {
                'v' -> termUI?.requirePaste()
                'n' -> termUI?.requireCreateNew()
                'z' -> termUI?.requireSwitchToPrevious()
                'x' -> termUI?.requireSwitchToNext()
                'f' -> termUI?.requireToggleFullScreen()
                '-' -> changeFontSize(false)
                '+' -> changeFontSize(true)
            }

            // 当要触发 NeoTerm 快捷键时，屏蔽所有终端处理key
            return true
        } else if (e != null && e.isAltPressed) {
            // Get the unmodified code point:
            val unicodeChar = e.getUnicodeChar(0).toChar()
            if (unicodeChar !in ('1'..'9')) {
                return false
            }

            // Use Alt + num to switch sessions
            val sessionIndex = unicodeChar.toInt() - '0'.toInt()
            termUI?.requireSwitchTo(sessionIndex)

            // 当要触发 NeoTerm 快捷键时，屏蔽所有终端处理key
            return true
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

        val shellSession = termData?.termSession as ShellTermSession? ?: return false

        // Volume keys as special keys
        val volumeAsSpecialKeys = shellSession.shellProfile.enableSpecialVolumeKeys

        val inputDevice = event.device
        if (inputDevice != null && inputDevice.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) {
            return false
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mVirtualControlKeyDown = down && volumeAsSpecialKeys
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mVirtualFnKeyDown = down && volumeAsSpecialKeys
            return true
        }
        return false
    }

    fun updateExtraKeys(title: String?, force: Boolean = false) {
        val extraKeysView = termData?.extraKeysView

        if (extraKeysView == null || title == null || title.isEmpty()) {
            return
        }

        if ((lastTitle != title || force)
                && updateExtraKeysVisibility()) {
            removeExtraKeys()
            ComponentManager.getComponent<ExtraKeysComponent>().showShortcutKeys(title, extraKeysView)
            extraKeysView.updateButtons()
            lastTitle = title
        }
    }

    private fun updateExtraKeysVisibility(): Boolean {
        val extraKeysView = termData?.extraKeysView ?: return false
        val shellSession = termData?.termSession as ShellTermSession? ?: return false

        return if (shellSession.shellProfile.enableExtraKeys) {
            extraKeysView.visibility = View.VISIBLE
            true
        } else {
            extraKeysView.visibility = View.GONE
            false
        }
    }

    private fun removeExtraKeys() {
        val extraKeysView = termData?.extraKeysView
        extraKeysView?.clearUserKeys()
    }

    private fun changeFontSize(increase: Boolean) {
        val termView = termData?.termView
        if (termView != null) {
            val changedSize = (if (increase) 1 else -1) * 2
            val fontSize = NeoPreference.validateFontSize(termView.textSize + changedSize)
            termView.textSize = fontSize
            NeoPreference.store(NeoPreference.KEY_FONT_SIZE, fontSize)
        }
    }
}