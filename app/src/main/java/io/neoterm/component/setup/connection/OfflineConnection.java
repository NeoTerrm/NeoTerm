package io.neoterm.component.setup.connection;

import java.io.IOException;
import java.io.InputStream;

import io.neoterm.App;
import io.neoterm.component.setup.SetupHelper;
import io.neoterm.component.setup.SourceConnection;
import io.neoterm.utils.AssetsUtils;

/**
 * @author kiva
 */

public abstract class OfflineConnection implements SourceConnection {
    private InputStream inputStream;

    protected abstract InputStream openInputStream() throws IOException;

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = openInputStream();
        }
        return inputStream;
    }

    @Override
    public int getSize() {
        if (inputStream != null) {
            try {
                return inputStream.available();
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }
}
