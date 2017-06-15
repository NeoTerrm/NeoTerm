package io.neoterm.utils

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * @author kiva
 */
object FileUtils {
    fun writeFile(path: File, bytes: ByteArray): Boolean {
        var output: OutputStream? = null
        var success = true
        try {
            output = FileOutputStream(path)
            output.write(bytes)
            output.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        } finally {
            if (output != null) {
                try {
                    output.close()
                } catch (ignore: Exception) {
                }
            }
        }
        return success
    }
}