package io.neoterm.setup.connections

import android.content.Context
import android.net.Uri

import java.io.IOException
import java.io.InputStream

/**
 * @author kiva
 */

open class OfflineUriConnection(private val context: Context, private val uri: Uri) : OfflineConnection() {

    @Throws(IOException::class)
    override fun openInputStream(): InputStream {
        return context.contentResolver.openInputStream(uri)
    }
}
