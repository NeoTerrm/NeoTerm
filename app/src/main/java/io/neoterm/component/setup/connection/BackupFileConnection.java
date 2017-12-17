package io.neoterm.component.setup.connection;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kiva
 */

public class BackupFileConnection extends OfflineConnection {
    private final String filePath;

    public BackupFileConnection(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected InputStream openInputStream() throws IOException {
        return null;
    }
}
