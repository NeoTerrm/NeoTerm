package io.neoterm.customize

import android.annotation.SuppressLint

/**
 * @author kiva
 */
object NeoTermPath {
    @SuppressLint("SdCardPath")
    const val ROOT_PATH = "/data/data/io.neoterm/files"
    const val USR_PATH =  "$ROOT_PATH/usr"
    const val HOME_PATH = "$ROOT_PATH/home"

    const val EKS_PATH = "$USR_PATH/share/eks"
    const val EKS_DEFAULT_FILE = "$EKS_PATH/default.eks"

    const val SERVER_BASE_URL = "http://mirror.neoterm.studio"
    const val SERVER_BOOT_URL = "$SERVER_BASE_URL/boot"
}