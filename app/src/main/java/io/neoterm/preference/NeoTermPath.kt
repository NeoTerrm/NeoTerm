package io.neoterm.preference

import android.annotation.SuppressLint
import io.neoterm.BuildConfig

/**
 * @author kiva
 */
object NeoTermPath {
    @SuppressLint("SdCardPath")
    const val ROOT_PATH = "/data/data/io.neoterm/files"
    const val USR_PATH = "${ROOT_PATH}/usr"
    const val HOME_PATH = "${ROOT_PATH}/home"

    const val CUSTOM_PATH = "${HOME_PATH}/.neoterm"
    const val EKS_PATH = "${CUSTOM_PATH}/eks"
    const val EKS_DEFAULT_FILE = "${EKS_PATH}/default.eks"
    const val FONT_PATH = "${CUSTOM_PATH}/font"
    const val COLORS_PATH = "${CUSTOM_PATH}/color"

    const val SOURCE_FILE = "${USR_PATH}/etc/apt/sources.list"
    const val PACKAGE_LIST_DIR = "${USR_PATH}/var/lib/apt/lists"

    private const val RELEASE_SOURCE = "https://mirrors.geekpie.org/neoterm"
    private const val DEBUG_SOURCE = "http://192.243.117.135"

    val DEFAULT_SOURCE: String
    val SERVER_BASE_URL: String
    val SERVER_BOOT_URL: String

    init {
        DEFAULT_SOURCE = if (BuildConfig.DEBUG) DEBUG_SOURCE else RELEASE_SOURCE
        SERVER_BASE_URL = DEFAULT_SOURCE
        SERVER_BOOT_URL = "${SERVER_BASE_URL}/boot"
    }
}