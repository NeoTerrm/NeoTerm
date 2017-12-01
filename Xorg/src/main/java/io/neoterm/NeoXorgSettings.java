package io.neoterm;

import io.neoterm.xorg.NeoXorgViewClient;

/**
 * @author kiva
 */

public class NeoXorgSettings {
    public static void init(NeoXorgViewClient client) {
        Settings.Load(client);
    }
}
