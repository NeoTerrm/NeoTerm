package io.neoterm.setup

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author kiva
 */

class BackupFileConnection(context: Context, uri: Uri) : LocalFileConnection(context, uri)

/**
 * @author kiva
 */

open class LocalFileConnection(context: Context, uri: Uri) : OfflineUriConnection(context, uri)

/**
 * @author kiva
 */

class NetworkConnection(private val sourceUrl: String) : SourceConnection {
  private var connection: HttpURLConnection? = null

  @Throws(IOException::class)
  override fun getInputStream(): InputStream {
    if (connection == null) {
      connection = openHttpConnection()
      connection!!.connectTimeout = 8000
      connection!!.readTimeout = 8000
    }
    return connection!!.inputStream
  }

  override fun getSize(): Int {
    return if (connection != null) {
      connection!!.contentLength
    } else 0

  }

  override fun close() {
    if (connection != null) {
      connection!!.disconnect()
    }
  }

  @Throws(IOException::class)
  private fun openHttpConnection(): HttpURLConnection {
    val arch = SetupHelper.determineArchName()

    return URL("$sourceUrl/boot/$arch.zip").openConnection() as HttpURLConnection
  }
}

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
      return try {
        inputStream!!.available()
      } catch (e: IOException) {
        e.printStackTrace()
        0
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

/**
 * @author kiva
 */

open class OfflineUriConnection(private val context: Context, private val uri: Uri) : OfflineConnection() {

  @Throws(IOException::class)
  override fun openInputStream(): InputStream {
    return context.contentResolver.openInputStream(uri)
  }
}
