package io.neoterm.component.setup.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kiva
 */

public class LocalFileConnection extends OfflineConnection {
    private final String filePath;

    public LocalFileConnection(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected InputStream openInputStream() throws IOException {
        return new FileInputStream(filePath);
    }
}
