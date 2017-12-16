package io.neoterm.component.setup.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.neoterm.R;
import io.neoterm.component.setup.SetupHelper;
import io.neoterm.component.setup.SourceConnection;
import io.neoterm.frontend.config.NeoPreference;
import io.neoterm.frontend.config.NeoTermPath;

/**
 * @author kiva
 */

public class NetworkConnection implements SourceConnection {
    private HttpURLConnection connection = null;

    @Override
    public InputStream getInputStream() throws IOException {
        if (connection == null) {
            connection = openBaseFileConnection();
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

    private static HttpURLConnection openBaseFileConnection() throws IOException {
        String arch = SetupHelper.determineArchName();
        String baseUrl = NeoTermPath.INSTANCE.getSERVER_BASE_URL();

        // Use the same source
        NeoPreference.INSTANCE.store(R.string.key_package_source, baseUrl);

        return (HttpURLConnection) new URL(baseUrl + "/boot/" + arch + ".zip").openConnection();
    }
}
