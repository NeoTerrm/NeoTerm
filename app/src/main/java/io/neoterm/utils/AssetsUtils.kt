package io.neoterm.utils

import android.content.Context
import java.io.File
import java.nio.file.Files

/**
 * @author kiva
 */
object AssetsUtils {
    fun extractAssetsDir(context: Context, dirName: String, extractDir: String) {
        val assets = context.assets
        assets.list(dirName)?.let {
            it.map { File(extractDir, it) }
                .takeWhile { !it.exists() }
                .forEach { file ->
                    assets.open("$dirName/${file.name}").use {
                        kotlin.runCatching { Files.copy(it, file.toPath()) }
                    }
                }
        }
    }
}