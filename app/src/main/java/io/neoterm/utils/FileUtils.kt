package io.neoterm.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * @author kiva
 */
object FileUtils {
    fun writeFile(path: File, bytes: ByteArray): Boolean {
        return FileOutputStream(path).use {
            it.write(bytes)
            it.flush()
            true
        }
    }

    fun writeFile(path: File, inputStream: InputStream): Boolean {
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes)
        return writeFile(path, bytes)
    }

    fun readFile(path: File): ByteArray? {
        if (!path.canRead()) {
            return null
        }

        return FileInputStream(path).use {
            val bytes = ByteArray(it.available())
            it.read(bytes)
            bytes
        }
    }
}