package io.neomodule.layout.attribute;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Map;

import io.neomodule.layout.abs.ImageLoader;
import io.neomodule.layout.abs.ViewAttributeRunnable;
import io.neomodule.layout.utils.DimensionConverter;

/**
 * @author kiva
 */

class ImageApply implements ViewAttributeRunnable {
    private ImageLoader imageLoader;

    ImageApply(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    @Override
    public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
        if (view instanceof ImageView) {
            String imageName = value;
            if (imageName.startsWith("//")) imageName = "http:" + imageName;
            if (imageName.startsWith("http")) {
                if (imageLoader != null) {
                    if (attrs.containsKey("cornerRadius")) {
                        int radius = DimensionConverter.toDimensionPixelSize(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
                        imageLoader.loadRoundedImage((ImageView) view, imageName, radius);
                    } else {
                        imageLoader.loadImage((ImageView) view, imageName);
                    }
                }

            } else if (imageName.startsWith("@drawable/")) {
                imageName = imageName.substring("@drawable/".length());
                ((ImageView) view).setImageDrawable(AttributeParser.getDrawableByName(view, imageName));
            }
        }
    }
}
