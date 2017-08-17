package io.neomodule.layout.abs;

import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

public interface ViewAttributeRunnable {
    void apply(View view, String value, ViewGroup parent, Map<String, String> attrs);
}
