package io.neomodule.layout.utils;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DimensionConverter {
    private static Map<String, Float> cached = new HashMap<>();

    // -- Initialize dimension string to constant lookup.
    private static final Map<String, Integer> dimensionConstantLookup = initDimensionConstantLookup();

    private static Map<String, Integer> initDimensionConstantLookup() {
        Map<String, Integer> m = new HashMap<>();
        m.put("px", TypedValue.COMPLEX_UNIT_PX);
        m.put("dip", TypedValue.COMPLEX_UNIT_DIP);
        m.put("dp", TypedValue.COMPLEX_UNIT_DIP);
        m.put("sp", TypedValue.COMPLEX_UNIT_SP);
        m.put("pt", TypedValue.COMPLEX_UNIT_PT);
        m.put("in", TypedValue.COMPLEX_UNIT_IN);
        m.put("mm", TypedValue.COMPLEX_UNIT_MM);
        return Collections.unmodifiableMap(m);
    }

    // -- Initialize pattern for dimension string.
    private static final Pattern DIMENSION_PATTERN = Pattern.compile("^\\s*(\\d+(\\.\\d+)*)\\s*([a-zA-Z]+)\\s*$");

    public static int toDimensionPixelSize(String dimension, DisplayMetrics metrics, ViewGroup parent, boolean horizontal) {
        if (dimension.endsWith("%")) {
            float pct = Float.parseFloat(dimension.substring(0, dimension.length() - 1)) / 100.0f;
            return (int) (pct * (horizontal ? parent.getMeasuredWidth() : parent.getMeasuredHeight()));
        }
        return toDimensionPixelSize(dimension, metrics);
    }

    public static int toDimensionPixelSize(String dimension, DisplayMetrics metrics) {
        // -- Mimics TypedValue.complexToDimensionPixelSize(int data, DisplayMetrics metrics).
        final float f;
        if (cached.containsKey(dimension)) {
            f = cached.get(dimension);
        } else {
            InternalDimension internalDimension = toInternalDimension(dimension);
            final float value = internalDimension.value;
            f = TypedValue.applyDimension(internalDimension.unit, value, metrics);
            cached.put(dimension, f);
        }
        final int res = (int) (f + 0.5f);
        if (res != 0) return res;
        if (f == 0) return 0;
        if (f > 0) return 1;
        return -1;
    }

    public static float toDimension(String dimension, DisplayMetrics metrics) {
        if (cached.containsKey(dimension)) return cached.get(dimension);
        // -- Mimics TypedValue.complexToDimension(int data, DisplayMetrics metrics).
        InternalDimension internalDimension = toInternalDimension(dimension);
        float val = TypedValue.applyDimension(internalDimension.unit, internalDimension.value, metrics);
        cached.put(dimension, val);
        return val;
    }

    private static InternalDimension toInternalDimension(String dimension) {
        // -- Match target against pattern.
        Matcher matcher = DIMENSION_PATTERN.matcher(dimension);
        if (matcher.matches()) {
            // -- Match found.
            // -- Extract value.
            float value = Float.valueOf(matcher.group(1));
            // -- Extract dimension units.
            String unit = matcher.group(3).toLowerCase();
            // -- Get Android dimension constant.
            Integer dimensionUnit = dimensionConstantLookup.get(unit);
            if (dimensionUnit == null) {
                // -- Invalid format.
                throw new NumberFormatException();
            } else {
                // -- Return valid dimension.
                return new InternalDimension(value, dimensionUnit);
            }
        } else {
            Log.e("DimensionConverter", "Invalid number format: " + dimension);
            // -- Invalid format.
            throw new NumberFormatException();
        }
    }

    private static class InternalDimension {
        float value;
        int unit;

        InternalDimension(float value, int unit) {
            this.value = value;
            this.unit = unit;
        }
    }
}
