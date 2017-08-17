package io.neomodule.layout.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import io.neomodule.layout.listener.OnClickForwarder;

/**
 * @author kiva
 */

public class AttributeParser {
    public static int adjustBrightness(int color, float amount) {
        int red = color & 0xFF0000 >> 16;
        int green = color & 0x00FF00 >> 8;
        int blue = color & 0x0000FF;
        int result = (int) (blue * amount);
        result += (int) (green * amount) << 8;
        result += (int) (red * amount) << 16;
        return result;
    }

    public static String parseId(String value) {
        if (value.startsWith("@+id/")) {
            return value.substring(5);
        } else if (value.startsWith("@id/")) {
            return value.substring(4);
        }
        return value;
    }

    public static int parseGravity(String value) {
        int gravity = Gravity.NO_GRAVITY;
        String[] parts = value.toLowerCase().split("[|]");
        for (String part : parts) {
            switch (part) {
                case "center":
                    gravity = gravity | Gravity.CENTER;
                    break;
                case "left":
                case "textStart":
                    gravity = gravity | Gravity.LEFT;
                    break;
                case "right":
                case "textEnd":
                    gravity = gravity | Gravity.RIGHT;
                    break;
                case "top":
                    gravity = gravity | Gravity.TOP;
                    break;
                case "bottom":
                    gravity = gravity | Gravity.BOTTOM;
                    break;
                case "center_horizontal":
                    gravity = gravity | Gravity.CENTER_HORIZONTAL;
                    break;
                case "center_vertical":
                    gravity = gravity | Gravity.CENTER_VERTICAL;
                    break;
            }
        }
        return gravity;
    }

    public static Drawable getDrawableByName(View view, String name) {
        Resources resources = view.getResources();
        return resources.getDrawable(resources.getIdentifier(name, "drawable",
                view.getContext().getPackageName()));
    }

    public static int parseColor(View view, String text) {
        if (text.startsWith("@color/")) {
            Resources resources = view.getResources();
            return resources.getColor(resources.getIdentifier(text.substring("@color/".length()), "color", view.getContext().getPackageName()));
        }
        if (text.length() == 4 && text.startsWith("#")) {
            text = "#" + text.charAt(1) + text.charAt(1) + text.charAt(2) + text.charAt(2) + text.charAt(3) + text.charAt(3);
        }
        return Color.parseColor(text);
    }

    public static View.OnClickListener parseOnClick(ViewGroup parent, String methodName) {
        return new OnClickForwarder(parent, methodName);
    }
}
