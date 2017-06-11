package io.neoterm

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import io.neoterm.terminal.TerminalSession
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

class MainActivity : Activity() {
    private lateinit var extraKeysView: ExtraKeysView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        extraKeysView = findViewById(R.id.extra_keys) as ExtraKeysView
        val view = findViewById(R.id.terminal_view) as TerminalView

        view.setBackgroundColor(Color.BLACK)
        view.textSize = 30
        view.setTypeface(Typeface.MONOSPACE)

        val session = TerminalSession("/system/bin/sh", "/",
                arrayOf("/system/bin/sh"),
                arrayOf("TERM=screen", "HOME=" + filesDir),
                object : TerminalSession.SessionChangedCallback {
                    override fun onBell(session: TerminalSession?) {
                    }

                    override fun onClipboardText(session: TerminalSession?, text: String?) {
                    }

                    override fun onColorsChanged(session: TerminalSession?) {
                    }

                    override fun onSessionFinished(finishedSession: TerminalSession?) {
                    }

                    override fun onTextChanged(changedSession: TerminalSession?) {
                        view.onScreenUpdated()
                    }

                    override fun onTitleChanged(changedSession: TerminalSession?) {
                    }
                })

        view.setOnKeyListener(object : TerminalViewClient {
            internal var mVirtualControlKeyDown: Boolean = false
            internal var mVirtualFnKeyDown: Boolean = false

            override fun onScale(scale: Float): Float {
                if (scale < 0.9f || scale > 1.1f) {
                    val increase = scale > 1f
                    val changedSize = (if (increase) 1 else -1) * 2
                    view.textSize = view.textSize + changedSize
                    return 1.0f
                }
                return scale
            }

            override fun onSingleTapUp(e: MotionEvent?) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
                return extraKeysView.readControlButton() || mVirtualControlKeyDown
            }

            override fun readAltKey(): Boolean {
                return extraKeysView.readAltButton() || mVirtualFnKeyDown
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

        })
        view.attachSession(session)
    }


}
