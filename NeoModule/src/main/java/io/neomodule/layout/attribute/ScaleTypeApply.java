package io.neomodule.layout.attribute;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Map;

import io.neomodule.layout.abs.ViewAttributeRunnable;

class ScaleTypeApply implements ViewAttributeRunnable {
        @Override
        public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
            if (view instanceof ImageView) {
                ImageView.ScaleType scaleType = ((ImageView) view).getScaleType();
                switch (value.toLowerCase()) {
                    case "center":
                        scaleType = ImageView.ScaleType.CENTER;
                        break;
                    case "center_crop":
                        scaleType = ImageView.ScaleType.CENTER_CROP;
                        break;
                    case "center_inside":
                        scaleType = ImageView.ScaleType.CENTER_INSIDE;
                        break;
                    case "fit_center":
                        scaleType = ImageView.ScaleType.FIT_CENTER;
                        break;
                    case "fit_end":
                        scaleType = ImageView.ScaleType.FIT_END;
                        break;
                    case "fit_start":
                        scaleType = ImageView.ScaleType.FIT_START;
                        break;
                    case "fit_xy":
                        scaleType = ImageView.ScaleType.FIT_XY;
                        break;
                    case "matrix":
                        scaleType = ImageView.ScaleType.MATRIX;
                        break;
                }
                ((ImageView) view).setScaleType(scaleType);
            }
        }
    }