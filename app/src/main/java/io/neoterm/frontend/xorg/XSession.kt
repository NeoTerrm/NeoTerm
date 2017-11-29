package io.neoterm.frontend.xorg

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.SystemClock
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import io.neoterm.*
import io.neoterm.xorg.NeoXorgViewClient
import io.neoterm.xorg.R
import java.util.*

/**
 * @author kiva
 */

class XSession private constructor(private val mActivity: Activity, private val sessionData: XSessionData) : NeoXorgViewClient {
    companion object {
        fun createSession(activity: Activity, parameter: XParameter): XSession {
            return XSession(activity, XSessionData())
        }
    }

    var mSessionName = "";

    init {
        if (Globals.InhibitSuspend) {
            window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (sessionData.audioThread == null) {
            sessionData.audioThread = NeoAudioThread(this)
        }
    }

    override fun getContext() = mActivity

    override fun isKeyboardWithoutTextInputShown() = sessionData.keyboardWithoutTextInputShown

    override fun isPaused() = sessionData.isPaused

    override fun runOnUiThread(runnable: Runnable?) = mActivity.runOnUiThread(runnable)

    override fun getGLView() = sessionData.glView

    override fun getWindow() = mActivity.window!!

    override fun getWindowManager() = mActivity.windowManager!!

    override fun showScreenKeyboardWithoutTextInputField(keyboard: Int) {
        val inputManager = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!isKeyboardWithoutTextInputShown) {
            sessionData.keyboardWithoutTextInputShown = true
            runOnUiThread(Runnable {
                if (keyboard == 0) {
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    inputManager.showSoftInput(glView, InputMethodManager.SHOW_FORCED)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                } else {
                    if (sessionData.screenKeyboard != null)
                        return@Runnable

                    val builtinKeyboard = BuiltInKeyboardView(mActivity, null)
                    builtinKeyboard.alpha = 0.7f
                    builtinKeyboard.changeKeyboard(keyboard)
                    builtinKeyboard.setOnKeyboardActionListener(object : KeyboardView.OnKeyboardActionListener {
                        override fun onPress(key: Int) {
                            var key = key
                            if (key == KeyEvent.KEYCODE_BACK)
                                return
                            if (key < 0)
                                return
                            for (k in builtinKeyboard.keyboard.keys) {
                                if (k.sticky && key == k.codes[0])
                                    return
                            }
                            if (key > 100000) {
                                key -= 100000
                                mActivity.onKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT))
                            }
                            mActivity.onKeyDown(key, KeyEvent(KeyEvent.ACTION_DOWN, key))
                        }

                        override fun onRelease(key: Int) {
                            var key = key
                            if (key == KeyEvent.KEYCODE_BACK) {
                                builtinKeyboard.setOnKeyboardActionListener(null)
                                showScreenKeyboardWithoutTextInputField(0) // Hide keyboard
                                return
                            }
                            if (key == Keyboard.KEYCODE_SHIFT) {
                                builtinKeyboard.shift = !builtinKeyboard.shift
                                if (builtinKeyboard.shift && !builtinKeyboard.alt)
                                    mActivity.onKeyDown(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT))
                                else
                                    mActivity.onKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
                                builtinKeyboard.changeKeyboard(keyboard)
                                return
                            }
                            if (key == Keyboard.KEYCODE_ALT) {
                                builtinKeyboard.alt = !builtinKeyboard.alt
                                if (builtinKeyboard.alt)
                                    mActivity.onKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
                                else
                                    builtinKeyboard.shift = false
                                builtinKeyboard.changeKeyboard(keyboard)
                                return
                            }
                            if (key < 0)
                                return
                            for (k in builtinKeyboard.keyboard.keys) {
                                if (k.sticky && key == k.codes[0]) {
                                    if (k.on) {
                                        builtinKeyboard.stickyKeys.add(key)
                                        mActivity.onKeyDown(key, KeyEvent(KeyEvent.ACTION_DOWN, key))
                                    } else {
                                        builtinKeyboard.stickyKeys.remove(key)
                                        mActivity.onKeyUp(key, KeyEvent(KeyEvent.ACTION_UP, key))
                                    }
                                    return
                                }
                            }

                            var shifted = false
                            if (key > 100000) {
                                key -= 100000
                                shifted = true
                            }

                            mActivity.onKeyUp(key, KeyEvent(KeyEvent.ACTION_UP, key))

                            if (shifted) {
                                mActivity.onKeyUp(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT))
                                builtinKeyboard.stickyKeys.remove(KeyEvent.KEYCODE_SHIFT_LEFT)
                                for (k in builtinKeyboard.keyboard.keys) {
                                    if (k.sticky && k.codes[0] == KeyEvent.KEYCODE_SHIFT_LEFT && k.on) {
                                        k.on = false
                                        builtinKeyboard.invalidateAllKeys()
                                    }
                                }
                            }
                        }

                        override fun onText(p1: CharSequence) {}

                        override fun swipeLeft() {}

                        override fun swipeRight() {}

                        override fun swipeDown() {}

                        override fun swipeUp() {}

                        override fun onKey(p1: Int, p2: IntArray) {}
                    })
                    sessionData.screenKeyboard = builtinKeyboard
                    val layout = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
                    sessionData.videoLayout!!.addView(sessionData.screenKeyboard, layout)
                }
            })
        } else {
            sessionData.keyboardWithoutTextInputShown = false
            runOnUiThread {
                if (sessionData.screenKeyboard != null) {
                    sessionData.videoLayout!!.removeView(sessionData.screenKeyboard)
                    sessionData.screenKeyboard = null
                }
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                inputManager.hideSoftInputFromWindow(glView!!.windowToken, 0)
            }
        }
        glView!!.callNativeScreenKeyboardShown(if (isKeyboardWithoutTextInputShown) 1 else 0)
    }

    override fun setScreenKeyboardHintMessage(hideMessage: String?) {
        sessionData.screenKeyboardHintMessage = hideMessage
        if (sessionData.screenKeyboard is EditText) {
            runOnUiThread {
                val editText = sessionData.screenKeyboard as EditText?
                editText?.hint = hideMessage ?: mActivity.getString(R.string.text_edit_click_here)
            }
        }
    }

    override fun isScreenKeyboardShown() = sessionData.screenKeyboard != null

    override fun showScreenKeyboard(oldText: String?) {
        if (Globals.CompatibilityHacksTextInputEmulatesHwKeyboard) {
            showScreenKeyboardWithoutTextInputField(Globals.TextInputKeyboard)
            return
        }
        if (sessionData.screenKeyboard != null)
            return

        val screenKeyboard = EditText(mActivity, null,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    android.R.style.TextAppearance_Material_Widget_EditText
                else android.R.style.TextAppearance_Widget_EditText)

        val hint = sessionData.screenKeyboardHintMessage
        screenKeyboard.hint = hint ?: mActivity.getString(R.string.text_edit_click_here)
        screenKeyboard.setText(oldText)
        screenKeyboard.setSelection(screenKeyboard.text.length)
        screenKeyboard.setOnKeyListener(SimpleKeyListener(this))
        screenKeyboard.setBackgroundColor(mActivity.resources.getColor(android.R.color.primary_text_light))
        screenKeyboard.setTextColor(mActivity.resources.getColor(android.R.color.background_light))

        if (isRunningOnOUYA && Globals.TvBorders)
            screenKeyboard.setPadding(100, 100, 100, 100) // Bad bad HDMI TVs all have cropped borders
        sessionData.screenKeyboard = screenKeyboard
        sessionData.videoLayout!!.addView(sessionData.screenKeyboard)

        screenKeyboard.inputType = InputType.TYPE_CLASS_TEXT
        screenKeyboard.isFocusableInTouchMode = true
        screenKeyboard.isFocusable = true
        screenKeyboard.postDelayed({
            screenKeyboard.requestFocus()
            screenKeyboard.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0f, 0f, 0))
            screenKeyboard.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0))
            screenKeyboard.postDelayed({
                screenKeyboard.requestFocus()
                screenKeyboard.setSelection(screenKeyboard.text.length)
            }, 100)
        }, 300)
    }

    override fun hideScreenKeyboard() {
        val inputManager = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (isKeyboardWithoutTextInputShown)
            showScreenKeyboardWithoutTextInputField(Globals.TextInputKeyboard)

        if (sessionData.screenKeyboard == null || sessionData.screenKeyboard !is EditText)
            return

        synchronized(sessionData.textInput) {
            val text = (sessionData.screenKeyboard as EditText).text.toString()
            for (i in 0 until text.length) {
                NeoRenderer.callNativeTextInput(text[i].toInt(), text.codePointAt(i))
            }
        }
        NeoRenderer.callNativeTextInputFinished()
        inputManager.hideSoftInputFromWindow(sessionData.screenKeyboard!!.windowToken, 0)
        sessionData.videoLayout!!.removeView(sessionData.screenKeyboard)
        sessionData.screenKeyboard = null
        glView!!.isFocusableInTouchMode = true
        glView!!.isFocusable = true
        glView!!.requestFocus()
    }

    override fun updateScreenOrientation() {
        var rotation: Int = windowManager.defaultDisplay.rotation
        NeoAccelerometerReader.setGyroInvertedOrientation(
                rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270)
    }

    override fun initScreenOrientation() {
        Globals.AutoDetectOrientation = true
        if (Globals.AutoDetectOrientation) {
            mActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            return
        }
        mActivity.requestedOrientation =
                if (Globals.HorizontalOrientation) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    override fun isRunningOnOUYA(): Boolean {
        try {
            mActivity.packageManager.getPackageInfo("tv.ouya", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }

        val uiModeManager = mActivity.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager?
        return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION || Globals.OuyaEmulation
    }

    override fun setSystemMousePointerVisible(visible: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            glView?.pointerIcon = android.view.PointerIcon.getSystemIcon(mActivity,
                    if (visible == 0) android.view.PointerIcon.TYPE_NULL
                    else android.view.PointerIcon.TYPE_DEFAULT)
        }
    }

    class SimpleKeyListener(var client: NeoXorgViewClient) : View.OnKeyListener {

        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == KeyEvent.KEYCODE_MENU ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_A ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_B ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_X ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_Y ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_1 ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_2 ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_3 ||
                    keyCode == KeyEvent.KEYCODE_BUTTON_4)) {
                client.hideScreenKeyboard()
                return true
            }
            return false
        }
    }

    class BuiltInKeyboardView(context: Context, attrs: android.util.AttributeSet?) : KeyboardView(context, attrs) {
        var shift = false
        var alt = false
        var stickyKeys = TreeSet<Int>()

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            if (ev.y < top)
                return false
            if (ev.action == MotionEvent.ACTION_DOWN || ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) {
                // Convert pointer coords, this will lose multitiouch data, however KeyboardView does not support multitouch anyway
                val converted = MotionEvent.obtain(ev.downTime, ev.eventTime, ev.action, ev.x, ev.y - top.toFloat(), ev.metaState)
                return super.dispatchTouchEvent(converted)
            }
            return false
        }

        override fun onKeyDown(key: Int, event: KeyEvent): Boolean {
            return false
        }

        override fun onKeyUp(key: Int, event: KeyEvent): Boolean {
            return false
        }

        fun changeKeyboard(keyboardIndex: Int) {
            val idx = (if (shift) 1 else 0) + if (alt) 2 else 0
            val keyboard = Keyboard(context, NeoTextInput.TextInputKeyboardList[idx][keyboardIndex])
            isPreviewEnabled = false
            isProximityCorrectionEnabled = false
            for (k in keyboard.keys) {
                if (stickyKeys.contains(k.codes[0])) {
                    k.on = true
                    invalidateAllKeys()
                }
            }
        }
    }

}
