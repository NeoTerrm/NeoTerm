package io.neomodule.layout;

import java.util.Map;

import io.neomodule.layout.abs.ImageLoader;
import io.neomodule.layout.abs.ViewAttributeRunnable;
import io.neomodule.layout.attribute.AttributeApply;

/**
 * @author kiva
 */

public class Configuration {
    public final int noLayoutRule = -999;
    public final String[] viewCorners = {"TopLeft", "TopRight", "BottomRight", "BottomLeft"};

    public Map<String, ViewAttributeRunnable> viewRunnables;
    public ImageLoader imageLoader = null;

    Configuration() {
    }

    void createViewRunnablesIfNeeded() {
        if (viewRunnables != null) {
            return;
        }
        viewRunnables = AttributeApply.declareDefaultApply(imageLoader);
    }
}
