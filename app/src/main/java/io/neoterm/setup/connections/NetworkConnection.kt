package io.neoterm.setup.connections

import io.neoterm.setup.SetupHelper
import io.neoterm.setup.SourceConnection
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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
