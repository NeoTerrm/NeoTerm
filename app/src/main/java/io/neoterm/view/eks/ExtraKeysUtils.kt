package io.neoterm.view.eks

import io.neoterm.customize.eks.ExtraKeyConfigParser
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
object ExtraKeysUtils {
    fun generateDefaultFile(defaultFile: File) {
        val DEFAULT_FILE_CONTENT = "version " + ExtraKeyConfigParser.PARSER_VERSION + "\n" +
                "program default\n" +
                "define - false\n" +
                "define / false\n" +
                "define \\ false\n" +
                "define | false\n" +
                "define $ false\n" +
                "define < false\n" +
                "define > false\n"
        FileUtils.writeFile(defaultFile, DEFAULT_FILE_CONTENT.toByteArray())
    }

    fun getDefaultFile(): File {
        val defaultFile = File(NeoTermPath.EKS_DEFAULT_FILE)
        if (!defaultFile.exists()) {
            generateDefaultFile(defaultFile)
        }
        return defaultFile
    }
}