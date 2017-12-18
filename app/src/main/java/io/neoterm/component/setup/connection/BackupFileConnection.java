package io.neoterm.component.setup.connection;

import android.content.Context;
import android.net.Uri;

/**
 * @author kiva
 */

public class BackupFileConnection extends OfflineUriConnection {

    public BackupFileConnection(Context context, Uri uri) {
        super(context, uri);
    }
}
