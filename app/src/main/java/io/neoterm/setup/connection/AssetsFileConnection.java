package io.neoterm.setup.connection;

import java.io.IOException;
import java.io.InputStream;

import io.neoterm.App;
import io.neoterm.setup.SetupHelper;
import io.neoterm.utils.AssetsUtils;

/**
 * @author kiva
 */

public class AssetsFileConnection extends OfflineConnection {
    @Override
    protected InputStream openInputStream() throws IOException {
        String arch = SetupHelper.INSTANCE.determineArchName();
        String fileName = "offline_setup/" + arch + ".zip";
        return AssetsUtils.INSTANCE.openAssetsFile(App.Companion.get(), fileName);
    }
}
