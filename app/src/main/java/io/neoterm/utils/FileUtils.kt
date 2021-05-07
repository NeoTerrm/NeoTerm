package io.neoterm.utils

import java.text.DecimalFormat

/**
 * @author kiva
 */
object FileUtils {
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