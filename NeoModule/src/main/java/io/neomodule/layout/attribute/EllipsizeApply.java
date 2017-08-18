package io.neomodule.layout.attribute;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class EllipsizeApply implements ViewAttributeRunnable {
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
}
