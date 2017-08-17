package io.neomodule.layout;

import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.neomodule.layout.abs.ImageLoader;
import io.neomodule.layout.abs.ViewAttributeRunnable;
import io.neomodule.layout.utils.AttributeParser;
import io.neomodule.layout.utils.DimensionConverter;

/**
 * @author kiva
 */

public class Configuration {
    public final int noLayoutRule = -999;
    public final String[] viewCorners = {"TopLeft", "TopRight", "BottomRight", "BottomLeft"};

    public Map<String, ViewAttributeRunnable> viewRunnables;
    public ImageLoader imageLoader = null;

    Configuration() {
    }

    void createViewRunnablesIfNeeded() {
        if (viewRunnables != null) {
            return;
        }
        viewRunnables = new HashMap<>(30);
        viewRunnables.put("scaleType", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof ImageView) {
                    ImageView.ScaleType scaleType = ((ImageView) view).getScaleType();
                    switch (value.toLowerCase()) {
                        case "center":
                            scaleType = ImageView.ScaleType.CENTER;
                            break;
                        case "center_crop":
                            scaleType = ImageView.ScaleType.CENTER_CROP;
                            break;
                        case "center_inside":
                            scaleType = ImageView.ScaleType.CENTER_INSIDE;
                            break;
                        case "fit_center":
                            scaleType = ImageView.ScaleType.FIT_CENTER;
                            break;
                        case "fit_end":
                            scaleType = ImageView.ScaleType.FIT_END;
                            break;
                        case "fit_start":
                            scaleType = ImageView.ScaleType.FIT_START;
                            break;
                        case "fit_xy":
                            scaleType = ImageView.ScaleType.FIT_XY;
                            break;
                        case "matrix":
                            scaleType = ImageView.ScaleType.MATRIX;
                            break;
                    }
                    ((ImageView) view).setScaleType(scaleType);
                }
            }
        });
        viewRunnables.put("orientation", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof LinearLayout) {
                    ((LinearLayout) view).setOrientation(value.equals("vertical") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                }
            }
        });
        viewRunnables.put("text", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(value);
                }
            }
        });
        viewRunnables.put("textSize", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, DimensionConverter.toDimension(value, view.getResources().getDisplayMetrics()));
                }
            }
        });
        viewRunnables.put("textColor", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(AttributeParser.parseColor(view, value));
                }
            }
        });
        viewRunnables.put("textStyle", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    int typeFace = Typeface.NORMAL;
                    if (value.contains("bold")) typeFace |= Typeface.BOLD;
                    else if (value.contains("italic")) typeFace |= Typeface.ITALIC;
                    ((TextView) view).setTypeface(null, typeFace);
                }
            }
        });
        viewRunnables.put("textAlignment", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    int alignment = View.TEXT_ALIGNMENT_TEXT_START;
                    switch (value) {
                        case "center":
                            alignment = View.TEXT_ALIGNMENT_CENTER;
                            break;
                        case "left":
                        case "textStart":
                            break;
                        case "right":
                        case "textEnd":
                            alignment = View.TEXT_ALIGNMENT_TEXT_END;
                            break;
                    }
                    view.setTextAlignment(alignment);
                } else {
                    int gravity = Gravity.LEFT;
                    switch (value) {
                        case "center":
                            gravity = Gravity.CENTER;
                            break;
                        case "left":
                        case "textStart":
                            break;
                        case "right":
                        case "textEnd":
                            gravity = Gravity.RIGHT;
                            break;
                    }
                    ((TextView) view).setGravity(gravity);
                }
            }
        });
        viewRunnables.put("ellipsize", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    TextUtils.TruncateAt where = TextUtils.TruncateAt.END;
                    switch (value) {
                        case "start":
                            where = TextUtils.TruncateAt.START;
                            break;
                        case "middle":
                            where = TextUtils.TruncateAt.MIDDLE;
                            break;
                        case "marquee":
                            where = TextUtils.TruncateAt.MARQUEE;
                            break;
                        case "end":
                            break;
                    }
                    ((TextView) view).setEllipsize(where);
                }
            }
        });
        viewRunnables.put("singleLine", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setSingleLine();
                }
            }
        });
        viewRunnables.put("hint", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof EditText) {
                    ((EditText) view).setHint(value);
                }
            }
        });
        viewRunnables.put("inputType", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    int inputType = 0;
                    switch (value) {
                        case "textEmailAddress":
                            inputType |= InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                            break;
                        case "number":
                            inputType |= InputType.TYPE_CLASS_NUMBER;
                            break;
                        case "phone":
                            inputType |= InputType.TYPE_CLASS_PHONE;
                            break;
                    }
                    if (inputType > 0) ((TextView) view).setInputType(inputType);
                }
            }
        });
        viewRunnables.put("gravity", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                int gravity = AttributeParser.parseGravity(value);
                if (view instanceof TextView) {
                    ((TextView) view).setGravity(gravity);
                } else if (view instanceof LinearLayout) {
                    ((LinearLayout) view).setGravity(gravity);
                } else if (view instanceof RelativeLayout) {
                    ((RelativeLayout) view).setGravity(gravity);
                }
            }
        });
        viewRunnables.put("src", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof ImageView) {
                    String imageName = value;
                    if (imageName.startsWith("//")) imageName = "http:" + imageName;
                    if (imageName.startsWith("http")) {
                        if (imageLoader != null) {
                            if (attrs.containsKey("cornerRadius")) {
                                int radius = DimensionConverter.toDimensionPixelSize(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
                                imageLoader.loadRoundedImage((ImageView) view, imageName, radius);
                            } else {
                                imageLoader.loadImage((ImageView) view, imageName);
                            }
                        }
                    } else if (imageName.startsWith("@drawable/")) {
                        imageName = imageName.substring("@drawable/".length());
                        ((ImageView) view).setImageDrawable(AttributeParser.getDrawableByName(view, imageName));
                    }
                }
            }
        });
        viewRunnables.put("visibility", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                int visibility = View.VISIBLE;
                String visValue = value.toLowerCase();
                if (visValue.equals("gone")) visibility = View.GONE;
                else if (visValue.equals("invisible")) visibility = View.INVISIBLE;
                view.setVisibility(visibility);
            }
        });
        viewRunnables.put("clickable", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                view.setClickable(value.equals("true"));
            }
        });
        viewRunnables.put("tag", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                throw new IllegalStateException("You cannot set tag in this situation, because we have other purpose.");
            }
        });
        viewRunnables.put("onClick", new ViewAttributeRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                view.setOnClickListener(AttributeParser.parseOnClick(parent, value));
            }
        });
    }
}
