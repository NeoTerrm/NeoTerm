package io.neoterm.frontend.xorg

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.Surface
import android.view.WindowManager
import io.neoterm.Globals
import io.neoterm.NeoAccelerometerReader
import io.neoterm.NeoAudioThread
import io.neoterm.xorg.NeoXorgViewClient

/**
 * @author kiva
 */

class XSession private constructor(val mActivity: Activity, val sessionData: XSessionData) : NeoXorgViewClient {
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

    override fun showScreenKeyboardWithoutTextInputField(flags: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setScreenKeyboardHintMessage(hideMessage: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isScreenKeyboardShown(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showScreenKeyboard(message: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideScreenKeyboard() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}
