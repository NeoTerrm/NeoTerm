package io.neoterm.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

/**
 * @author kiva
 */
object FileUtils {
    fun writeFile(path: File, bytes: ByteArray): Boolean {
        try {
            return FileOutputStream(path).use {
                it.write(bytes)
                it.flush()
                true
            }
        } catch (e: Exception) {
            return false
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

    fun formatSizeInKB(size: Long): String {
        val decimalFormat = DecimalFormat("####.00");
        if (size < 1024) {
            return "$size KB"
        } else if (size < 1024 * 1024) {
            val parsedSize: Float = size / 1024f
            return decimalFormat.format(parsedSize) + " MB"
        } else if (size < 1024 * 1024 * 1024) {
            val parsedSize: Float = size / 1024f / 1024f
            return decimalFormat.format(parsedSize) + " GB"
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            val parsedSize: Float = size / 1024f / 1024f / 1024f
            return decimalFormat.format(parsedSize) + " TB"
        } else {
            return "$size KB"
        }
    }
}