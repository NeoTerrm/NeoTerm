package io.neoterm.ui.bonus;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import io.neoterm.R;

/**
 * @author kiva
 */

public class BonusActivity extends AppCompatActivity {
    final static int[] FLAVORS = {
            0xFF9C27B0, 0xFFBA68C8, // grape
            0xFFFF9800, 0xFFFFB74D, // orange
            0xFFF06292, 0xFFF8BBD0, // bubblegum
            0xFFAFB42B, 0xFFCDDC39, // lime
            0xFF795548, 0xFFA1887F, // mystery flavor
    };

    FrameLayout mLayout;
    int mTapCount;
    int mKeyCount;
    PathInterpolator mInterpolator = new PathInterpolator(0f, 0f, 0.5f, 1f);

    static int newColorIndex() {
        return 2 * ((int) (Math.random() * FLAVORS.length / 2));
    }

    Drawable makeRipple() {
        final int idx = newColorIndex();
        final ShapeDrawable popbg = new ShapeDrawable(new OvalShape());
        popbg.getPaint().setColor(FLAVORS[idx]);
        final RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(FLAVORS[idx + 1]),
                popbg, null);
        return ripple;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new FrameLayout(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(mLayout);
    }

    @Override
    public void onAttachedToWindow() {
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        final float dp = dm.density;
        final int size = (int)
                (Math.min(Math.min(dm.widthPixels, dm.heightPixels), 600 * dp) - 100 * dp);
        final View stick = new View(this) {
            Paint mPaint = new Paint();
            Path mShadow = new Path();

            @Override
            public void onAttachedToWindow() {
                super.onAttachedToWindow();
                setWillNotDraw(false);
                setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRect(0, getHeight() / 2, getWidth(), getHeight());
                    }
                });
            }

            @Override
            public void onDraw(Canvas c) {
                final int w = c.getWidth();
                final int h = c.getHeight() / 2;
                c.translate(0, h);
                final GradientDrawable g = new GradientDrawable();
                g.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                g.setGradientCenter(w * 0.75f, 0);
                g.setColors(new int[]{0xFFFFFFFF, 0xFFAAAAAA});
                g.setBounds(0, 0, w, h);
                g.draw(c);
                mPaint.setColor(0xFFAAAAAA);
                mShadow.reset();
                mShadow.moveTo(0, 0);
                mShadow.lineTo(w, 0);
                mShadow.lineTo(w, size / 2 + 1.5f * w);
                mShadow.lineTo(0, size / 2);
                mShadow.close();
                c.drawPath(mShadow, mPaint);
            }
        };
        mLayout.addView(stick, new FrameLayout.LayoutParams((int) (32 * dp),
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL));
        stick.setAlpha(0f);

        final ImageView im = new ImageView(this);
        im.setTranslationZ(20);
        im.setScaleX(0);
        im.setScaleY(0);
        final Drawable platlogo = getDrawable(R.drawable.plat_logo);
        platlogo.setAlpha(0);
        im.setImageDrawable(platlogo);
        im.setBackground(makeRipple());
        im.setClickable(true);
        final ShapeDrawable highlight = new ShapeDrawable(new OvalShape());
        highlight.getPaint().setColor(0x10FFFFFF);
        highlight.setBounds((int) (size * .15f), (int) (size * .15f),
                (int) (size * .6f), (int) (size * .6f));
        im.getOverlay().add(highlight);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTapCount == 0) {
                    im.animate()
                            .translationZ(40)
                            .scaleX(1)
                            .scaleY(1)
                            .setInterpolator(mInterpolator)
                            .setDuration(700)
                            .setStartDelay(500)
                            .start();

                    final ObjectAnimator a = ObjectAnimator.ofInt(platlogo, "alpha", 0, 255);
                    a.setInterpolator(mInterpolator);
                    a.setStartDelay(1000);
                    a.start();

                    stick.animate()
                            .translationZ(20)
                            .alpha(1)
                            .setInterpolator(mInterpolator)
                            .setDuration(700)
                            .setStartDelay(750)
                            .start();
                } else {
                    im.setBackground(makeRipple());
                }
                mTapCount++;
            }
        });

        // Enable hardware keyboard input for TV compatibility.
        im.setFocusable(true);
        im.requestFocus();
        im.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    ++mKeyCount;
                    if (mKeyCount > 2) {
                        if (mTapCount > 5) {
                            im.performLongClick();
                        } else {
                            im.performClick();
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        mLayout.addView(im, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));

        im.animate().scaleX(0.3f).scaleY(0.3f)
                .setInterpolator(mInterpolator)
                .setDuration(500)
                .setStartDelay(800)
                .start();
    }
}
