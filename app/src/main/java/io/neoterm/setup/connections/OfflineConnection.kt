package io.neoterm.setup.connections

import io.neoterm.setup.SourceConnection
import java.io.IOException
import java.io.InputStream

/**
 * @author kiva
 */

abstract class OfflineConnection : SourceConnection {
    private var inputStream: InputStream? = null

    @Throws(IOException::class)
    protected abstract fun openInputStream(): InputStream

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        if (inputStream == null) {
            inputStream = openInputStream()
        }
        return inputStream!!
    }

    override fun getSize(): Int {
        if (inputStream != null) {
            try {
                return inputStream!!.available()
            } catch (e: IOException) {
                e.printStackTrace()
                return 0
            }

        }
        return 0
    }

    override fun close() {
        if (inputStream != null) {
            try {
                inputStream!!.close()
            } catch (ignore: IOException) {
                ignore.printStackTrace()
            }

        }
    }
}
