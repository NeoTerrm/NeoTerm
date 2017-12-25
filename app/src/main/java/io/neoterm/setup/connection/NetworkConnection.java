package io.neoterm.setup.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.neoterm.setup.SetupHelper;
import io.neoterm.setup.SourceConnection;

/**
 * @author kiva
 */

public class NetworkConnection implements SourceConnection {
    private final String sourceUrl;
    private HttpURLConnection connection = null;

    public NetworkConnection(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (connection == null) {
            connection = openHttpConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
        }
        return connection.getInputStream();
    }

    @Override
    public int getSize() {
        if (connection != null) {
            return connection.getContentLength();
        }

        return 0;
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private HttpURLConnection openHttpConnection() throws IOException {
        String arch = SetupHelper.INSTANCE.determineArchName();

        return (HttpURLConnection) new URL(sourceUrl + "/boot/" + arch + ".zip").openConnection();
    }
}
