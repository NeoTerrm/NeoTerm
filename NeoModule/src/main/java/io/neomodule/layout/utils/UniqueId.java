package io.neomodule.layout.utils;

import android.view.View;
import android.view.ViewGroup;

import io.neomodule.layout.LayoutInfo;

/**
 * @author kiva
 */

public class UniqueId {
    private static int ID = 52019;

    public static int newId() {
        return ID++;
    }

    public static int idFromString(View view, String id) {
        if (!(view instanceof ViewGroup)) return 0;
        Object tag = view.getTag();
        if (!(tag instanceof LayoutInfo)) return 0; // not inflated by this class
        LayoutInfo info = (LayoutInfo) view.getTag();
        if (!info.nameToIdNumber.containsKey(id)) {
            ViewGroup grp = (ViewGroup) view;
            for (int i = 0; i < grp.getChildCount(); i++) {
                int val = idFromString(grp.getChildAt(i), id);
                if (val != 0) return val;
            }
            return 0;
        }
        return info.nameToIdNumber.get(id);
    }
}
