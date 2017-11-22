package com.termux.component.pm

import com.termux.R
import com.termux.frontend.logging.NLog
import com.termux.frontend.preference.NeoPreference
import com.termux.frontend.preference.NeoTermPath
import java.io.File
import java.net.URL

/**
 * @author Sam
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

            if (url.path != null && url.path.isNotEmpty()) {
                builder.append("_")
                builder.append(url.path.substring(1)) // Skip '/'
            }
            builder.append("_dists_stable_main_binary-")
            return builder.toString()
        } catch (e: Exception) {
            NLog.e("PM", "Failed to detect source file prefix: ${e.localizedMessage}")
            return ""
        }
    }
}
