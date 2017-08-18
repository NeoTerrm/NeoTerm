package io.neomodule.layout.attribute;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;
import io.neomodule.layout.utils.DimensionConverter;

/**
 * @author kiva
 */

class TextSizeApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, DimensionConverter.toDimension(value, view.getResources().getDisplayMetrics()));
        }
    }
}
