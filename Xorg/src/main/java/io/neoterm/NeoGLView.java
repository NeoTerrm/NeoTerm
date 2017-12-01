package io.neoterm;

import io.neoterm.xorg.NeoXorgViewClient;

/**
 * @author kiva
 */

public class NeoGLView extends DemoGLSurfaceView {
    public NeoGLView(NeoXorgViewClient client) {
        super(client);
    }

    public void callNativeScreenKeyboardShown(int shown) {
        nativeScreenKeyboardShown(shown);
    }

    public void callNativeScreenVisibleRect(int x, int y, int w, int h) {
        nativeScreenVisibleRect(x, y, w, h);
    }
}
