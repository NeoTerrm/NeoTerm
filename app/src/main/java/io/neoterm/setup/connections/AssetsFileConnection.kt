package io.neoterm.setup.connections

import io.neoterm.App
import io.neoterm.setup.SetupHelper
import io.neoterm.utils.AssetsUtils
import java.io.IOException
import java.io.InputStream

/**
 * @author kiva
 */

class AssetsFileConnection : OfflineConnection() {
    @Throws(IOException::class)
    override fun openInputStream(): InputStream {
        val arch = SetupHelper.determineArchName()
        val fileName = "offline_setup/$arch.zip"
        return AssetsUtils.openAssetsFile(App.get(), fileName)
    }
}
