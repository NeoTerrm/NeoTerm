package io.neoterm.customize.shortcut.builtin

import io.neoterm.customize.NeoTermPath
import io.neoterm.customize.shortcut.ShortcutConfigParser
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
object BuiltinShortcutKeys {
    private const val vimKeys = "version ${ShortcutConfigParser.PARSER_VERSION}\n" +
            "program vim neovim vi\n" +
            "define / false\n" +
            "define :w true\n" +
            "define dd true\n" +
            "define :q true\n"

    private const val moreKeys = "version ${ShortcutConfigParser.PARSER_VERSION}\n" +
            "program more less\n" +
            "define R false\n" +
            "define Q false\n"

    fun registerAll() {
        val vimFile = File(NeoTermPath.EKS_PATH, "vim.eks")
        if (!vimFile.exists()) {
            FileUtils.writeFile(vimFile, vimKeys.toByteArray())
        }

        val moreFile = File(NeoTermPath.EKS_PATH, "more-less.eks")
        if (!moreFile.exists()) {
            FileUtils.writeFile(moreFile, moreKeys.toByteArray())
        }
    }
}