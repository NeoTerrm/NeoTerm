package io.neoterm.frontend.preference

import android.annotation.SuppressLint

/**
 * @author kiva
 */
object NeoTermPath {
    @SuppressLint("SdCardPath")
    const val ROOT_PATH = "/data/data/io.neoterm/files"
    const val USR_PATH = "$ROOT_PATH/usr"
    const val HOME_PATH = "$ROOT_PATH/home"
    const val APT_BIN_PATH = "$USR_PATH/bin/apt"
    const val LIB_PATH = "$USR_PATH/lib"

    const val CUSTOM_PATH = "$HOME_PATH/.neoterm"
    const val NEOTERM_SHELL_PATH = "$CUSTOM_PATH/shell"
    const val EKS_PATH = "$CUSTOM_PATH/eks"
    const val EKS_DEFAULT_FILE = "$EKS_PATH/default.nl"
    const val FONT_PATH = "$CUSTOM_PATH/font"
    const val COLORS_PATH = "$CUSTOM_PATH/color"
    const val USER_SCRIPT_PATH = "$CUSTOM_PATH/script"

    const val SOURCE_FILE = "$USR_PATH/etc/apt/sources.list"
    const val PACKAGE_LIST_DIR = "$USR_PATH/var/lib/apt/lists"

    const val SOURCE = "http://neoterm.studio"

    val DEFAULT_SOURCE: String
    val SERVER_BASE_URL: String
    val SERVER_BOOT_URL: String

    init {
        DEFAULT_SOURCE = SOURCE
        SERVER_BASE_URL = DEFAULT_SOURCE
        SERVER_BOOT_URL = "$SERVER_BASE_URL/boot"
    }
}