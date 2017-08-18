package io.neomodule.layout.attribute;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class TextStyleApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        if (view instanceof TextView) {
            int typeFace = Typeface.NORMAL;
            if (value.contains("bold")) typeFace |= Typeface.BOLD;
            else if (value.contains("italic")) typeFace |= Typeface.ITALIC;
            ((TextView) view).setTypeface(null, typeFace);
        }
    }
}
