package io.neoterm.utils

import android.content.Context
import java.io.File
import java.io.InputStream

/**
 * @author kiva
 */
object AssetsUtils {
    fun extractAssetsDir(context: Context, dirName: String, extractDir: String) {
        val assets = context.assets
        assets.list(dirName)
                .map { File(extractDir, it) }
                .takeWhile { !it.exists() }
                .forEach { file ->
                    assets.open("$dirName/${file.name}").use {
                        FileUtils.writeFile(file, it)
                    }
                }
    }

    fun openAssetsFile(context: Context, fileName: String) : InputStream {
        val assets = context.assets
        return assets.open(fileName)
    }
}