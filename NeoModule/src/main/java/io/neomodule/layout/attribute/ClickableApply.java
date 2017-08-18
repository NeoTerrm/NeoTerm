package io.neomodule.layout.attribute;

import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class ClickableApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        view.setClickable(value.equals("true"));
    }
}
