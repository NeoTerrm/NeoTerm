package io.neoterm.utils

import android.content.Context
import java.io.File

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
}