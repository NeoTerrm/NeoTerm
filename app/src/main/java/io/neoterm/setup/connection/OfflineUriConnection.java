package io.neoterm.setup.connection;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kiva
 */

public class OfflineUriConnection extends OfflineConnection {
    private final Context context;
    private final Uri uri;

    public OfflineUriConnection(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    protected InputStream openInputStream() throws IOException {
        return context.getContentResolver().openInputStream(uri);
    }
}
