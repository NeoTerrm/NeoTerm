package io.neomodule.layout.attribute;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class SingleLineApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        if (view instanceof TextView) {
            ((TextView) view).setSingleLine();
        }
    }
}
