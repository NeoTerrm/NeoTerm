package io.neomodule.layout.attribute;

import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

/**
 * @author kiva
 */

class VisibilityApply implements ViewAttributeRunnable {
    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        int visibility = View.VISIBLE;
        String visValue = value.toLowerCase();
        if (visValue.equals("gone")) visibility = View.GONE;
        else if (visValue.equals("invisible")) visibility = View.INVISIBLE;
        view.setVisibility(visibility);
    }
}
