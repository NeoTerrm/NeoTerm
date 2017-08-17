package io.neomodule.layout;

import android.graphics.drawable.GradientDrawable;

import java.util.HashMap;

public class LayoutInfo {
    public HashMap<String, Integer> nameToIdNumber;
    public Object delegate;
    public GradientDrawable backgroundDrawable;

    public LayoutInfo() {
        nameToIdNumber = new HashMap<>();
    }
}