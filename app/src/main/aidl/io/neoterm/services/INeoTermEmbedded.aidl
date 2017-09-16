// INeoTermEmbedded.aidl
package io.neoterm.services;

import android.os.ResultReceiver;

// Declare any non-default types here with import statements

interface INeoTermEmbedded {
    // write command text to terminal
    void writeToTerminal(String sessionName, String command);

    void onCreate(String sessionName, in ResultReceiver resultReceiver);
    void onVisible(String sessionName);
    void layoutView(String sessionName, int x, int y, int width, int height);
    void onInVisible(String sessionName);
    void onDestroyed(String sessionName);
}
