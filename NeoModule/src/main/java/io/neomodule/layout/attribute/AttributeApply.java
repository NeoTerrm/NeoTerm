package io.neomodule.layout.attribute;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import io.neomodule.layout.Configuration;
import io.neomodule.layout.LayoutInfo;
import io.neomodule.layout.abs.ImageLoader;
import io.neomodule.layout.abs.ViewAttributeRunnable;
import io.neomodule.layout.utils.DimensionConverter;
import io.neomodule.layout.utils.UniqueId;

/**
 * @author kiva
 */

public class AttributeApply {
    /**
     * 存储处理中的状态
     */
    private static class Status {
        int layoutRule;
        int marginLeft = 0;
        int marginRight = 0;
        int marginTop = 0;
        int marginBottom = 0;
        int paddingLeft = 0;
        int paddingRight = 0;
        int paddingTop = 0;
        int paddingBottom = 0;
        boolean hasCornerRadius = false;
        boolean hasCornerRadii = false;
        boolean layoutTarget = false;

        Status(Configuration config) {
            this.layoutRule = config.noLayoutRule;
        }
    }

    private View view;
    private Map<String, String> attrs;
    private ViewGroup parent;

    public AttributeApply(View view, Map<String, String> attrs, ViewGroup parent) {
        this.view = view;
        this.attrs = attrs;
        this.parent = parent;
    }

    /**
     * 把xml里定义的属性设置到 View 中
     *
     * @param config LayoutInflater的配置
     */
    public void apply(Configuration config) {
        Status status = new Status(config);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }

        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            String attr = entry.getKey();
            // 如果存在预设的处理方式，我们就不管了
            if (config.viewRunnables.containsKey(attr)) {
                config.viewRunnables.get(attr).apply(view, entry.getValue(), parent, attrs);
                continue;
            }

            if (attr.startsWith("cornerRadius")) {
                status.hasCornerRadius = true;
                status.hasCornerRadii = !attr.equals("cornerRadius");
                continue;
            }

            applyAttribute(attr, entry, layoutParams, status);

            if (status.layoutRule != config.noLayoutRule && parent instanceof RelativeLayout) {
                if (status.layoutTarget) {
                    int anchor = UniqueId.idFromString(parent, AttributeParser.parseId(entry.getValue()));
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(status.layoutRule, anchor);
                } else if (entry.getValue().equals("true")) {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(status.layoutRule);
                }
            }
        }

        // 处理 View 的背景
        if (attrs.containsKey("background") || attrs.containsKey("borderColor")) {
            String backgroundValue = attrs.containsKey("background") ? attrs.get("background") : null;

            // 如果直接从 drawable 中拿那就简单多了
            if (backgroundValue != null && backgroundValue.startsWith("@drawable/")) {
                applyBackgroundDrawable(backgroundValue);

            } else if (backgroundValue == null
                    || backgroundValue.startsWith("#")
                    || backgroundValue.startsWith("@color")) {
                applyBackgroundColor(config, status, backgroundValue);
            }
        }

        applyParsedMargin(status, layoutParams);
        applyParsedPadding(status);
        view.setLayoutParams(layoutParams);
    }

    /**
     * 解析每一个属性
     *
     * @param attr         属性名
     * @param entry        属性的名和值
     * @param layoutParams 父视图的 LayoutParams
     * @param status       处理状态
     */
    private void applyAttribute(String attr, Map.Entry<String, String> entry,
                                ViewGroup.LayoutParams layoutParams, Status status) {
        if (attr.startsWith("layout_margin")) {
            applyLayoutMargin(attr, entry, status);
            return;
        }

        if (attr.startsWith("padding")) {
            applyPadding(attr, entry, status);
            return;
        }

        switch (attr) {
            case "id":
                applyId(entry);
                break;
            case "width":
            case "layout_width":
                applyLayoutWidth(layoutParams, entry);
                break;
            case "height":
            case "layout_height":
                applyLayoutHeight(layoutParams, entry);
                break;
            case "layout_gravity":
                applyGravity(layoutParams, entry);
                break;
            case "layout_weight":
                applyLayoutWeight(layoutParams, entry);
                break;
            case "layout_below":
                status.layoutRule = RelativeLayout.BELOW;
                status.layoutTarget = true;
                break;
            case "layout_above":
                status.layoutRule = RelativeLayout.ABOVE;
                status.layoutTarget = true;
                break;
            case "layout_toLeftOf":
                status.layoutRule = RelativeLayout.LEFT_OF;
                status.layoutTarget = true;
                break;
            case "layout_toRightOf":
                status.layoutRule = RelativeLayout.RIGHT_OF;
                status.layoutTarget = true;
                break;
            case "layout_alignBottom":
                status.layoutRule = RelativeLayout.ALIGN_BOTTOM;
                status.layoutTarget = true;
                break;
            case "layout_alignTop":
                status.layoutRule = RelativeLayout.ALIGN_TOP;
                status.layoutTarget = true;
                break;
            case "layout_alignLeft":
            case "layout_alignStart":
                status.layoutRule = RelativeLayout.ALIGN_LEFT;
                status.layoutTarget = true;
                break;
            case "layout_alignRight":
            case "layout_alignEnd":
                status.layoutRule = RelativeLayout.ALIGN_RIGHT;
                status.layoutTarget = true;
                break;
            case "layout_alignParentBottom":
                status.layoutRule = RelativeLayout.ALIGN_PARENT_BOTTOM;
                break;
            case "layout_alignParentTop":
                status.layoutRule = RelativeLayout.ALIGN_PARENT_TOP;
                break;
            case "layout_alignParentLeft":
            case "layout_alignParentStart":
                status.layoutRule = RelativeLayout.ALIGN_PARENT_LEFT;
                break;
            case "layout_alignParentRight":
            case "layout_alignParentEnd":
                status.layoutRule = RelativeLayout.ALIGN_PARENT_RIGHT;
                break;
            case "layout_centerHorizontal":
                status.layoutRule = RelativeLayout.CENTER_HORIZONTAL;
                break;
            case "layout_centerVertical":
                status.layoutRule = RelativeLayout.CENTER_VERTICAL;
                break;
            case "layout_centerInParent":
                status.layoutRule = RelativeLayout.CENTER_IN_PARENT;
                break;
        }
    }

    /**
     * 从 status 中读取处理完毕的 padding 系列值，并且应用到 View 上
     *
     * @param status 处理状态
     */
    private void applyParsedPadding(Status status) {
        view.setPadding(status.paddingLeft, status.paddingTop, status.paddingRight, status.paddingBottom);
    }

    /**
     * 从 status 中读取处理完毕的 layout_margin 系列值，并且应用到 View 上
     *
     * @param status       处理状态
     * @param layoutParams 父视图的 LayoutParams
     */
    private void applyParsedMargin(Status status, ViewGroup.LayoutParams layoutParams) {
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(status.marginLeft,
                    status.marginTop,
                    status.marginRight,
                    status.marginBottom);
        }
    }

    /**
     * 从当前 app 的 drawable 中得到背景并设置到 View 上
     *
     * @param drawableResourceName 资源在 drawable 下的名字
     */
    private void applyBackgroundDrawable(String drawableResourceName) {
        view.setBackground(AttributeParser.getDrawableByName(view, drawableResourceName));
    }

    /**
     * 解析一个 # 开头的颜色值或从当前app 的 R.color 下取得颜色应用于 View 的背景颜色
     *
     * @param config     LayoutInflater 配置
     * @param status     处理状态
     * @param colorValue # 或者 @color/ 开头的颜色值
     */
    private void applyBackgroundColor(Configuration config, Status status, String colorValue) {
        int validatedColor = AttributeParser.parseColor(view, colorValue == null ? "#00000000" : colorValue);

        if (view instanceof Button || attrs.containsKey("pressedColor")) {
            // 按钮有按下效果，所以我们视为同一种情况
            applyPressedColor(config, status, validatedColor);

        } else if (status.hasCornerRadius || attrs.containsKey("borderColor")) {
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(validatedColor);

            // 把 pressed 和正常视为同一种情况
            applyCorners(config, status, gd, gd);
            applyBorderColor(gd, gd);

            view.setBackground(gd);
            getLayoutInfo().backgroundDrawable = gd;

        } else {
            view.setBackgroundColor(validatedColor);
        }
    }

    /**
     * 处理 pressedColor 或者 Button 的背景色
     *
     * @param config     LayoutInflater 配置
     * @param status     出炉状态
     * @param colorValue 颜色值
     */
    private void applyPressedColor(Configuration config, Status status, int colorValue) {
        int pressedColor;

        if (attrs.containsKey("pressedColor")) {
            pressedColor = AttributeParser.parseColor(view, attrs.get("pressedColor"));
        } else {
            pressedColor = AttributeParser.adjustBrightness(colorValue, 0.9f);
        }

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(colorValue);
        GradientDrawable pressedGd = new GradientDrawable();
        pressedGd.setColor(pressedColor);

        applyCorners(config, status, gd, pressedGd);
        applyBorderColor(gd, pressedGd);

        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, pressedGd);
        selector.addState(new int[]{}, gd);
        view.setBackground(selector);

        getLayoutInfo().backgroundDrawable = gd;
    }

    /**
     * 处理 borderColor
     *
     * @param gd        背景
     * @param pressedGd 按下时的背景，如果没有，设置为 gd 即可
     */
    private void applyBorderColor(GradientDrawable gd, GradientDrawable pressedGd) {
        if (attrs.containsKey("borderColor")) {
            String borderWidth = "1dp";
            if (attrs.containsKey("borderWidth")) {
                borderWidth = attrs.get("borderWidth");
            }
            int borderWidthPx = DimensionConverter.toDimensionPixelSize(borderWidth, view.getResources().getDisplayMetrics());
            gd.setStroke(borderWidthPx, AttributeParser.parseColor(view, attrs.get("borderColor")));
            pressedGd.setStroke(borderWidthPx, AttributeParser.parseColor(view, attrs.get("borderColor")));
        }
    }

    /**
     * 处理 cornerRadius 或者 cornerRadiusXXX
     *
     * @param config    LayoutInflater 配置
     * @param status    出炉状态
     * @param gd        背景
     * @param pressedGd 按下背景，如果没有，设置为 gd 即可
     */
    private void applyCorners(Configuration config, Status status, GradientDrawable gd, GradientDrawable pressedGd) {
        if (status.hasCornerRadii) {
            float radii[] = new float[8];
            for (int i = 0; i < config.viewCorners.length; i++) {
                String corner = config.viewCorners[i];
                if (attrs.containsKey("cornerRadius" + corner)) {
                    radii[i * 2] = radii[i * 2 + 1] = DimensionConverter.toDimension(attrs.get("cornerRadius" + corner), view.getResources().getDisplayMetrics());
                }
                gd.setCornerRadii(radii);
                pressedGd.setCornerRadii(radii);
            }

        } else if (status.hasCornerRadius) {
            float cornerRadius = DimensionConverter.toDimension(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
            gd.setCornerRadius(cornerRadius);
            pressedGd.setCornerRadius(cornerRadius);
        }
    }

    /**
     * 设置 xml 属性中的 padding 系列属性，如 padding="10dp", paddingLeft="16dp"
     *
     * @param attr   属性名，需要判断是 padding 还是 paddingXXX
     * @param entry  属性值
     * @param status 处理状态
     */
    private void applyPadding(String attr, Map.Entry<String, String> entry, Status status) {
        switch (attr) {
            case "padding":
                status.paddingBottom = status.paddingLeft = status.paddingRight = status.paddingTop
                        = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;
            case "paddingLeft":
                status.paddingLeft = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;
            case "paddingTop":
                status.paddingTop = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;
            case "paddingRight":
                status.paddingRight = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;
            case "paddingBottom":
                status.paddingBottom = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;

        }
    }

    /**
     * 设置 xml 属性中的 layout_margin 系列属性，如 layout_margin="10dp", layout_marginRight="16dp"
     *
     * @param attr   属性名，需要判断是 layout_margin 还是 layout_marginXXX
     * @param entry  属性值
     * @param status 处理状态
     */
    private void applyLayoutMargin(String attr, Map.Entry<String, String> entry, Status status) {
        switch (attr) {
            case "layout_margin":
                status.marginLeft = status.marginRight = status.marginTop = status.marginBottom
                        = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics());
                break;
            case "layout_marginLeft":
                status.marginLeft = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics(), parent, true);
                break;
            case "layout_marginTop":
                status.marginTop = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics(), parent, false);
                break;
            case "layout_marginRight":
                status.marginRight = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics(), parent, true);
                break;
            case "layout_marginBottom":
                status.marginBottom = DimensionConverter.toDimensionPixelSize(entry.getValue(),
                        view.getResources().getDisplayMetrics(), parent, false);
                break;
        }
    }

    /**
     * 设置 layout_weight
     *
     * @param layoutParams 父视图的 LayoutParams
     * @param entry        属性值
     */
    private void applyLayoutWeight(ViewGroup.LayoutParams layoutParams, Map.Entry<String, String> entry) {
        if (parent != null && parent instanceof LinearLayout) {
            ((LinearLayout.LayoutParams) layoutParams).weight = Float.parseFloat(entry.getValue());
        }
    }

    /**
     * 设置 gravity
     *
     * @param layoutParams 父视图的 LayoutParams
     * @param entry        属性值
     */
    private void applyGravity(ViewGroup.LayoutParams layoutParams, Map.Entry<String, String> entry) {
        if (parent != null && parent instanceof LinearLayout) {
            ((LinearLayout.LayoutParams) layoutParams).gravity = AttributeParser.parseGravity(entry.getValue());
        } else if (parent != null && parent instanceof FrameLayout) {
            ((FrameLayout.LayoutParams) layoutParams).gravity = AttributeParser.parseGravity(entry.getValue());
        }
    }

    /**
     * 设置 layout_height
     *
     * @param layoutParams 父视图的 LayoutParams
     * @param entry        属性值
     */
    private void applyLayoutHeight(ViewGroup.LayoutParams layoutParams, Map.Entry<String, String> entry) {
        switch (entry.getValue()) {
            case "wrap_content":
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                break;
            case "fill_parent":
            case "match_parent":
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
            default:
                layoutParams.height = DimensionConverter.toDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, false);
                break;
        }
    }

    /**
     * 设置 layout_width
     *
     * @param layoutParams 父视图的 LayoutParams
     * @param entry        属性值
     */
    private void applyLayoutWidth(ViewGroup.LayoutParams layoutParams, Map.Entry<String, String> entry) {
        switch (entry.getValue()) {
            case "wrap_content":
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                break;
            case "fill_parent":
            case "match_parent":
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
            default:
                layoutParams.width = DimensionConverter.toDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, true);
                break;
        }
    }

    /**
     * 设置 id
     *
     * @param entry 属性值
     */
    private void applyId(Map.Entry<String, String> entry) {
        String idString = AttributeParser.parseId(entry.getValue());
        if (parent != null) {
            LayoutInfo info = getLayoutInfo();
            int newId = UniqueId.newId();
            view.setId(newId);
            info.nameToIdNumber.put(idString, newId);
        }
    }

    private LayoutInfo getLayoutInfo() {
        LayoutInfo info;
        if (parent.getTag() != null && parent.getTag() instanceof LayoutInfo) {
            info = (LayoutInfo) parent.getTag();
        } else {
            info = new LayoutInfo();
            parent.setTag(info);
        }
        return info;
    }

    public static Map<String, ViewAttributeRunnable> declareDefaultApply(final ImageLoader imageLoader) {
        Map<String, ViewAttributeRunnable> viewRunnables = new HashMap<>(30);

        viewRunnables.put("scaleType", new ScaleTypeApply());
        viewRunnables.put("orientation", new OrientationApply());
        viewRunnables.put("text", new TextApply());
        viewRunnables.put("textSize", new TextSizeApply());
        viewRunnables.put("textColor", new TextColorApply());
        viewRunnables.put("textStyle", new TextStyleApply());
        viewRunnables.put("textAlignment", new TextAlignmentApply());
        viewRunnables.put("ellipsize", new EllipsizeApply());
        viewRunnables.put("singleLine", new SingleLineApply());
        viewRunnables.put("hint", new HintApply());
        viewRunnables.put("inputType", new InputTypeApply());
        viewRunnables.put("gravity", new GravityApply());
        viewRunnables.put("src", new ImageApply(imageLoader));
        viewRunnables.put("visibility", new VisibilityApply());
        viewRunnables.put("clickable", new ClickableApply());
        viewRunnables.put("tag", new TagApply());
        viewRunnables.put("onClick", new ClickableApply());

        return viewRunnables;
    }
}
