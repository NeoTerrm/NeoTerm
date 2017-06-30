package io.neoterm.customize.eks.builtin

import io.neoterm.customize.NeoTermPath
import io.neoterm.customize.eks.EksConfigParser
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
object BuiltinEksKeys {
    private const val vimKeys = "version ${EksConfigParser.PARSER_VERSION}\n" +
            "program vim neovim vi\n" +
            "define dd true\n" +
            "define :x true\n" +
            "define :w true\n" +
            "define :q true\n"

    private const val moreKeys = "version ${EksConfigParser.PARSER_VERSION}\n" +
            "program more less\n" +
            "define R false\n" +
            "define Q false\n"

    fun registerAll() {
        val configDir = File(NeoTermPath.EKS_PATH)
        configDir.mkdirs()

        val vimFile = File(configDir, "vim.eks")
        if (!vimFile.exists()) {
            FileUtils.writeFile(vimFile, vimKeys.toByteArray())
        }

        val moreFile = File(configDir, "more-less.eks")
        if (!moreFile.exists()) {
            FileUtils.writeFile(moreFile, moreKeys.toByteArray())
        }
    }
}