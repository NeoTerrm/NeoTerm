package io.neoterm.component.config

import android.annotation.SuppressLint
import android.os.Process

object DefaultValues {
  const val fontSize = 30

  const val enableBell = false
  const val enableVibrate = false
  const val enableExecveWrapper = true
  const val enableAutoCompletion = false
  const val enableFullScreen = false
  const val enableAutoHideToolbar = false
  const val enableSwitchNextTab = false
  const val enableExtraKeys = true
  const val enableExplicitExtraKeysWeight = false
  const val enableBackButtonBeMappedToEscape = false
  const val enableSpecialVolumeKeys = false
  const val enableWordBasedIme = false

  const val loginShell = "bash"
  const val initialCommand = ""
  const val defaultFont = "SourceCodePro"
}

object NeoTermPath {
  @SuppressLint("SdCardPath")
  val USER_DATA_PATH = "/data/user/${Process.myUid() / 100000}"
  @JvmField val ROOT_PATH = "${USER_DATA_PATH}/io.neoterm/files"
  val USR_PATH = "$ROOT_PATH/usr"
  val HOME_PATH = "$ROOT_PATH/home"
  val APT_BIN_PATH = "$USR_PATH/bin/apt"
  val LIB_PATH = "$USR_PATH/lib"

  val CUSTOM_PATH = "$HOME_PATH/.neoterm"
  val NEOTERM_LOGIN_SHELL_PATH = "$CUSTOM_PATH/shell"
  val EKS_PATH = "$CUSTOM_PATH/eks"
  val EKS_DEFAULT_FILE = "$EKS_PATH/default.nl"
  val FONT_PATH = "$CUSTOM_PATH/font"
  val COLORS_PATH = "$CUSTOM_PATH/color"
  val USER_SCRIPT_PATH = "$CUSTOM_PATH/script"
  val PROFILE_PATH = "$CUSTOM_PATH/profile"

  var SOURCE_FILE = "$USR_PATH/etc/apt/sources.list"
  var PACKAGE_LIST_DIR = "$USR_PATH/var/lib/apt/lists"

  private const val SOURCE = "https://raw.githubusercontent.com/NeoTerm/NeoTerm-repo/main"

  val DEFAULT_MAIN_PACKAGE_SOURCE: String

  init {
    DEFAULT_MAIN_PACKAGE_SOURCE = SOURCE
  }
}
