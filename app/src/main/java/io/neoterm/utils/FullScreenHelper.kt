package io.neoterm.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout

/**
 * Helper class to "adjustResize" Activity when we are in full screen mode and check IME status.
 * Android Bug 5497: https://code.google.com/p/android/issues/detail?id=5497
 */
class FullScreenHelper private constructor(activity: Activity, var fullScreen: Boolean, private var shouldSkipFirst: Boolean) {

    interface KeyBoardListener {
        /**
         * call back

         * @param isShow         true is show else hidden
         * *
         * @param keyboardHeight keyboard height
         */
        fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int)
    }

    private val mChildOfContent: View
    private var usableHeightPrevious: Int = 0
    private val frameLayoutParams: FrameLayout.LayoutParams

    private var mOriginHeight: Int = 0
    private var mPreHeight: Int = 0
    private var mKeyBoardListener: KeyBoardListener? = null

    fun setKeyBoardListener(mKeyBoardListener: KeyBoardListener) {
        this.mKeyBoardListener = mKeyBoardListener
    }

    init {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        mChildOfContent = content.getChildAt(0)
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (this@FullScreenHelper.fullScreen) {
                possiblyResizeChildOfContent()
            }
            monitorImeStatus()
        }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }

    private fun monitorImeStatus() {
        val currHeight = mChildOfContent.height
        if (currHeight == 0 && shouldSkipFirst) {
            // First time
            return
        }

        shouldSkipFirst = false
        var hasChange = false
        if (mPreHeight == 0) {
            mPreHeight = currHeight
            mOriginHeight = currHeight
        } else {
            if (mPreHeight != currHeight) {
                hasChange = true
                mPreHeight = currHeight
            } else {
                hasChange = false
            }
        }
        if (hasChange) {
            var keyboardHeight = 0
            val keyBoardIsShowing: Boolean
            if (Math.abs(mOriginHeight - currHeight) < 100) {
                //hidden
                keyBoardIsShowing = false
            } else {
                //show
                keyboardHeight = mOriginHeight - currHeight
                keyBoardIsShowing = true
            }

            if (mKeyBoardListener != null) {
                mKeyBoardListener!!.onKeyboardChange(keyBoardIsShowing, keyboardHeight)
            }
        }
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        val currentHeightLayoutHeight: Int

        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                currentHeightLayoutHeight = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                currentHeightLayoutHeight = usableHeightSansKeyboard
            }
            frameLayoutParams.height = currentHeightLayoutHeight
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top
    }

    companion object {
        fun injectActivity(activity: Activity, fullScreen: Boolean, recreate: Boolean): FullScreenHelper {
            return FullScreenHelper(activity, fullScreen, recreate)
        }

        fun getStatusBarHeight(context: Context): Int {
            val resourceId = context.resources.getIdentifier("status_bar_height",
                    "dimen", "android")
            if (resourceId > 0) {
                return context.resources.getDimensionPixelSize(resourceId)
            }
            return -1
        }
    }
}