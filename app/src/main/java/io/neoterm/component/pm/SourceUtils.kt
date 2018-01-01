package io.neoterm.component.pm

import io.neoterm.R
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
import java.io.File
import java.net.URL

/**
 * @author kiva
 */
object SourceUtils {

    fun detectSourceFiles(): ArrayList<File> {
        val sourceFiles = ArrayList<File>()
        try {
            val sourceUrl = NeoPreference.loadString(R.string.key_package_source, NeoTermPath.DEFAULT_SOURCE)
            val packageFilePrefix = detectSourceFilePrefix(sourceUrl)
            if (packageFilePrefix.isNotEmpty()) {
                File(NeoTermPath.PACKAGE_LIST_DIR)
                        .listFiles()
                        .filterTo(sourceFiles) { it.name.startsWith(packageFilePrefix) }
            }
        } catch (e: Exception) {
            sourceFiles.clear()
            NLog.e("PM", "Failed to detect source files: ${e.localizedMessage}")
        }

        return sourceFiles
    }

    fun detectSourceFilePrefix(sourceUrl: String): String {
        try {
            val url = URL(sourceUrl)
            val builder = StringBuilder()
            builder.append(url.host)
            // https://github.com/NeoTerm/NeoTerm/issues/1
            if (url.port != -1) {
                builder.append(":${url.port}")
            }

            val path = url.path
            if (path != null && path.isNotEmpty()) {
                builder.append("_")
                val fixedPath = path.replace("/", "_")
                        .substring(1) // skip the last '/'
                builder.append(fixedPath)
            }
            builder.append("_dists_stable_main_binary-")
            return builder.toString()
        } catch (e: Exception) {
            NLog.e("PM", "Failed to detect source file prefix: ${e.localizedMessage}")
            return ""
        }
    }
}