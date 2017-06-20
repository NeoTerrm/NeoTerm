package io.neoterm.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Helper class to "adjustResize" Activity when we are in full screen mode and check IME status.
 * Android Bug 5497: https://code.google.com/p/android/issues/detail?id=5497
 */
public class FullScreenHelper {
    public static FullScreenHelper injectActivity(Activity activity, boolean fullScreen, boolean recreate) {
        return new FullScreenHelper(activity, fullScreen, recreate);
    }

    public interface KeyBoardListener {
        /**
         * call back
         *
         * @param isShow         true is show else hidden
         * @param keyboardHeight keyboard height
         */
        void onKeyboardChange(boolean isShow, int keyboardHeight);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private int mOriginHeight;
    private int mPreHeight;
    private KeyBoardListener mKeyBoardListener;
    private boolean fullScreen;
    private boolean shouldSkipFirst;

    public void setKeyBoardListener(KeyBoardListener mKeyBoardListener) {
        this.mKeyBoardListener = mKeyBoardListener;
    }

    private FullScreenHelper(Activity activity, final boolean fullScreen, boolean recreate) {
        this.fullScreen = fullScreen;
        this.shouldSkipFirst = recreate;
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (FullScreenHelper.this.fullScreen) {
                    possiblyResizeChildOfContent();
                }
                monitorImeStatus();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void monitorImeStatus() {
        int currHeight = mChildOfContent.getHeight();
        if (currHeight == 0 && shouldSkipFirst) {
            // First time
            return;
        }

        shouldSkipFirst = false;
        boolean hasChange = false;
        if (mPreHeight == 0) {
            mPreHeight = currHeight;
            mOriginHeight = currHeight;
        } else {
            if (mPreHeight != currHeight) {
                hasChange = true;
                mPreHeight = currHeight;
            } else {
                hasChange = false;
            }
        }
        if (hasChange) {
            int keyboardHeight = 0;
            boolean keyBoardIsShowing;
            if (Math.abs(mOriginHeight - currHeight) < 100) {
                //hidden
                keyBoardIsShowing = false;
            } else {
                //show
                keyboardHeight = mOriginHeight - currHeight;
                keyBoardIsShowing = true;
            }

            if (mKeyBoardListener != null) {
                mKeyBoardListener.onKeyboardChange(keyBoardIsShowing, keyboardHeight);
            }
        }
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        int currentHeightLayoutHeight;

        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                currentHeightLayoutHeight = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden
                currentHeightLayoutHeight = usableHeightSansKeyboard;
            }
            frameLayoutParams.height = currentHeightLayoutHeight;
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom - r.top;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }
}