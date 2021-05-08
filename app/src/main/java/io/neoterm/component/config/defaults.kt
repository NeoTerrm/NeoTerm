package io.neoterm.component.config

import android.annotation.SuppressLint

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
  const val ROOT_PATH = "/data/data/io.neoterm/files"
  const val USR_PATH = "$ROOT_PATH/usr"
  const val HOME_PATH = "$ROOT_PATH/home"
  const val APT_BIN_PATH = "$USR_PATH/bin/apt"
  const val LIB_PATH = "$USR_PATH/lib"

  const val CUSTOM_PATH = "$HOME_PATH/.neoterm"
  const val NEOTERM_LOGIN_SHELL_PATH = "$CUSTOM_PATH/shell"
  const val EKS_PATH = "$CUSTOM_PATH/eks"
  const val EKS_DEFAULT_FILE = "$EKS_PATH/default.nl"
  const val FONT_PATH = "$CUSTOM_PATH/font"
  const val COLORS_PATH = "$CUSTOM_PATH/color"
  const val USER_SCRIPT_PATH = "$CUSTOM_PATH/script"
  const val PROFILE_PATH = "$CUSTOM_PATH/profile"

  const val SOURCE_FILE = "$USR_PATH/etc/apt/sources.list"
  const val PACKAGE_LIST_DIR = "$USR_PATH/var/lib/apt/lists"

  private const val SOURCE = "https://raw.githubusercontent.com/NeoTerm/NeoTerm-repo/main"

  val DEFAULT_MAIN_PACKAGE_SOURCE: String

  init {
    DEFAULT_MAIN_PACKAGE_SOURCE = SOURCE
  }
}
