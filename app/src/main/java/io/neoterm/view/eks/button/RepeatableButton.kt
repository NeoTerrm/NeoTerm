package io.neoterm.view.eks.button

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button


/**
 * @author kiva
 */
open class RepeatableButton(buttonText: String) : ControlButton(buttonText) {

    override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
        return RepeatableButtonWidget(context, attrs, defStyleAttr)
    }

    private class RepeatableButtonWidget(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
        : Button(context, attrs, defStyleAttr) {

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
