package io.neoterm.frontend.session.xorg

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
import io.neoterm.frontend.session.xorg.client.XSessionData
import io.neoterm.xorg.NeoXorgViewClient
import io.neoterm.xorg.R
import java.util.*

/**
 * @author kiva
 */

class XSession constructor(private val mActivity: Activity, val mSessionData: XSessionData) : NeoXorgViewClient {
    var mSessionName = "";

    init {
        if (Globals.InhibitSuspend) {
            window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        mSessionData.client = this
        NeoXorgSettings.init(this)
        if (mSessionData.audioThread == null) {
            mSessionData.audioThread = NeoAudioThread(this)
        }
    }

    fun onPause() {
        mSessionData.isPaused = true
        if (mSessionData.glView != null) {
            mSessionData.glView?.onPause()
        }
    }

    fun onDestroy() {
        if (mSessionData.glView != null) {
        mSessionData.glView?.exitApp()
        }
    }

    fun onResume() {
        if (mSessionData.glView != null) {
            mSessionData.glView?.onResume()
        }
        mSessionData.isPaused = false
    }

    override fun getContext() = mActivity

    override fun isKeyboardWithoutTextInputShown() = mSessionData.keyboardWithoutTextInputShown

    override fun isPaused() = mSessionData.isPaused

    override fun runOnUiThread(runnable: Runnable?) = mActivity.runOnUiThread(runnable)

    override fun getGLView() = mSessionData.glView

    override fun getWindow() = mActivity.window!!

    override fun getWindowManager() = mActivity.windowManager!!

    override fun showScreenKeyboardWithoutTextInputField(keyboard: Int) {
        val inputManager = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!isKeyboardWithoutTextInputShown) {
            mSessionData.keyboardWithoutTextInputShown = true
            runOnUiThread(Runnable {
                if (keyboard == 0) {
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    inputManager.showSoftInput(glView, InputMethodManager.SHOW_FORCED)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                } else {
                    if (mSessionData.screenKeyboard != null)
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
                    mSessionData.screenKeyboard = builtinKeyboard
                    val layout = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM)
                    mSessionData.videoLayout!!.addView(mSessionData.screenKeyboard, layout)
                }
            })
        } else {
            mSessionData.keyboardWithoutTextInputShown = false
            runOnUiThread {
                if (mSessionData.screenKeyboard != null) {
                    mSessionData.videoLayout!!.removeView(mSessionData.screenKeyboard)
                    mSessionData.screenKeyboard = null
                }
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                inputManager.hideSoftInputFromWindow(glView!!.windowToken, 0)
            }
        }
        glView!!.callNativeScreenKeyboardShown(if (isKeyboardWithoutTextInputShown) 1 else 0)
    }

    override fun setScreenKeyboardHintMessage(hideMessage: String?) {
        mSessionData.screenKeyboardHintMessage = hideMessage
        if (mSessionData.screenKeyboard is EditText) {
            runOnUiThread {
                val editText = mSessionData.screenKeyboard as EditText?
                editText?.hint = hideMessage ?: mActivity.getString(R.string.text_edit_click_here)
            }
        }
    }

    override fun isScreenKeyboardShown() = mSessionData.screenKeyboard != null

    override fun showScreenKeyboard(oldText: String?) {
        if (Globals.CompatibilityHacksTextInputEmulatesHwKeyboard) {
            showScreenKeyboardWithoutTextInputField(Globals.TextInputKeyboard)
            return
        }
        if (mSessionData.screenKeyboard != null)
            return

        val screenKeyboard = EditText(mActivity, null,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    android.R.style.TextAppearance_Material_Widget_EditText
                else android.R.style.TextAppearance_Widget_EditText)

        val hint = mSessionData.screenKeyboardHintMessage
        screenKeyboard.hint = hint ?: mActivity.getString(R.string.text_edit_click_here)
        screenKeyboard.setText(oldText)
        screenKeyboard.setSelection(screenKeyboard.text.length)
        screenKeyboard.setOnKeyListener(SimpleKeyListener(this))
        screenKeyboard.setBackgroundColor(mActivity.resources.getColor(android.R.color.primary_text_light))
        screenKeyboard.setTextColor(mActivity.resources.getColor(android.R.color.background_light))

        if (isRunningOnOUYA && Globals.TvBorders)
            screenKeyboard.setPadding(100, 100, 100, 100) // Bad bad HDMI TVs all have cropped borders
        mSessionData.screenKeyboard = screenKeyboard
        mSessionData.videoLayout!!.addView(mSessionData.screenKeyboard)

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

        if (mSessionData.screenKeyboard == null || mSessionData.screenKeyboard !is EditText)
            return

        synchronized(mSessionData.textInput) {
            val text = (mSessionData.screenKeyboard as EditText).text.toString()
            for (i in 0 until text.length) {
                NeoRenderer.callNativeTextInput(text[i].toInt(), text.codePointAt(i))
            }
        }
        NeoRenderer.callNativeTextInputFinished()
        inputManager.hideSoftInputFromWindow(mSessionData.screenKeyboard!!.windowToken, 0)
        mSessionData.videoLayout!!.removeView(mSessionData.screenKeyboard)
        mSessionData.screenKeyboard = null
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
