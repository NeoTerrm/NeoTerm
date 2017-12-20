package io.neoterm.setup.connection;

import android.content.Context;
import android.net.Uri;

/**
 * @author kiva
 */

public class LocalFileConnection extends OfflineUriConnection {

    public LocalFileConnection(Context context, Uri uri) {
        super(context, uri);
    }
}
