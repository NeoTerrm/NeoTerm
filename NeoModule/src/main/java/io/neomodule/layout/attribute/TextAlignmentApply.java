package io.neomodule.layout.attribute;

import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class TextAlignmentApply implements ViewAttributeRunnable {
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
}
