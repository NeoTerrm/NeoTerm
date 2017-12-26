package io.neoterm.ui.bonus

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import io.neoterm.R

/**
 * @author kiva
 */

class BonusActivity : AppCompatActivity() {

    lateinit internal var mLayout: FrameLayout
    internal var mTapCount: Int = 0
    internal var mKeyCount: Int = 0
    internal var mInterpolator = PathInterpolator(0f, 0f, 0.5f, 1f)

    internal fun makeRipple(): Drawable {
        val idx = newColorIndex()
        val lollipopBackground = ShapeDrawable(OvalShape())
        lollipopBackground.paint.color = FLAVORS[idx]
        return RippleDrawable(
                ColorStateList.valueOf(FLAVORS[idx + 1]),
                lollipopBackground, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLayout = FrameLayout(this)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(mLayout)
    }

    override fun onAttachedToWindow() {
        val dm = resources.displayMetrics
        val dp = dm.density
        val size = (Math.min(Math.min(dm.widthPixels, dm.heightPixels).toFloat(), 600 * dp) - 100 * dp).toInt()
        val stick = object : View(this) {
            internal var mPaint = Paint()
            internal var mShadow = Path()

            public override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                setWillNotDraw(false)
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRect(0, height / 2, width, height)
                    }
                }
            }

            public override fun onDraw(c: Canvas) {
                val w = c.width
                val h = c.height / 2
                c.translate(0f, h.toFloat())
                val g = GradientDrawable()
                g.orientation = GradientDrawable.Orientation.LEFT_RIGHT
                g.setGradientCenter(w * 0.75f, 0f)
                g.colors = intArrayOf(0xFFFFFFFF.toInt(), 0xFFAAAAAA.toInt())
                g.setBounds(0, 0, w, h)
                g.draw(c)
                mPaint.color = 0xFFAAAAAA.toInt()
                mShadow.reset()
                mShadow.moveTo(0f, 0f)
                mShadow.lineTo(w.toFloat(), 0f)
                mShadow.lineTo(w.toFloat(), size / 2 + 1.5f * w)
                mShadow.lineTo(0f, (size / 2).toFloat())
                mShadow.close()
                c.drawPath(mShadow, mPaint)
            }
        }
        mLayout.addView(stick, FrameLayout.LayoutParams((32 * dp).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL))
        stick.alpha = 0f

        val im = ImageView(this)
        im.translationZ = 20f
        im.scaleX = 0f
        im.scaleY = 0f
        val platlogo = getDrawable(R.drawable.plat_logo)
        platlogo!!.alpha = 0
        im.setImageDrawable(platlogo)
        im.background = makeRipple()
        im.isClickable = true
        val highlight = ShapeDrawable(OvalShape())
        highlight.paint.color = 0x10FFFFFF
        highlight.setBounds((size * .15f).toInt(), (size * .15f).toInt(),
                (size * .6f).toInt(), (size * .6f).toInt())
        im.overlay.add(highlight)
        im.setOnClickListener {
            if (mTapCount == 0) {
                im.animate()
                        .translationZ(40f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(mInterpolator)
                        .setDuration(700)
                        .setStartDelay(500)
                        .start()

                val a = ObjectAnimator.ofInt(platlogo, "alpha", 0, 255)
                a.interpolator = mInterpolator
                a.startDelay = 1000
                a.start()

                stick.animate()
                        .translationZ(20f)
                        .alpha(1f)
                        .setInterpolator(mInterpolator)
                        .setDuration(700)
                        .setStartDelay(750)
                        .start()
            } else {
                im.background = makeRipple()
            }
            mTapCount++
        }

        // Enable hardware keyboard input for TV compatibility.
        im.isFocusable = true
        im.requestFocus()
        im.setOnKeyListener { v, keyCode, event ->
            if (keyCode != KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                ++mKeyCount
                if (mKeyCount > 2) {
                    if (mTapCount > 5) {
                        im.performLongClick()
                    } else {
                        im.performClick()
                    }
                }
                true
            } else {
                false
            }
        }

        mLayout.addView(im, FrameLayout.LayoutParams(size, size, Gravity.CENTER))

        im.animate().scaleX(0.3f).scaleY(0.3f)
                .setInterpolator(mInterpolator)
                .setDuration(500)
                .setStartDelay(800)
                .start()
    }

    companion object {
        internal val FLAVORS = intArrayOf(0xFF9C27B0.toInt(), 0xFFBA68C8.toInt(), // grape
                0xFFFF9800.toInt(), 0xFFFFB74D.toInt(), // orange
                0xFFF06292.toInt(), 0xFFF8BBD0.toInt(), // bubblegum
                0xFFAFB42B.toInt(), 0xFFCDDC39.toInt(), // lime
                0xFF795548.toInt(), 0xFFA1887F.toInt())// mystery flavor

        internal fun newColorIndex(): Int {
            return 2 * (Math.random() * FLAVORS.size / 2).toInt()
        }
    }
}
