package io.neoterm;

import android.app.Application;

/**
 * @author kiva
 */

public class NeoApp extends Application {
    private static NeoApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static NeoApp get() {
        return app;
    }
}
