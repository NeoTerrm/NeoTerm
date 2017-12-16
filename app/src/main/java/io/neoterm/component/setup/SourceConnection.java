package io.neoterm.component.setup;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kiva
 */

public interface SourceConnection {
    InputStream getInputStream() throws IOException;

    int getSize();

    void close();
}
