package io.neoterm.frontend.session.shell

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.component.profile.NeoProfile
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.DefaultValues
import io.neoterm.frontend.config.NeoPreference

/**
 * @author kiva
 */
class ShellProfile : NeoProfile() {
  companion object {
    const val PROFILE_META_NAME = "profile-shell"

    private const val LOGIN_SHELL = "login-shell"
    private const val INITIAL_COMMAND = "init-command"
    private const val BELL = "bell"
    private const val VIBRATE = "vibrate"
    private const val EXECVE_WRAPPER = "execve-wrapper"
    private const val SPECIAL_VOLUME_KEYS = "special-volume-keys"
    private const val AUTO_COMPLETION = "auto-completion"
    private const val BACK_KEY_TO_ESC = "back-key-esc"
    private const val EXTRA_KEYS = "extra-keys"
    private const val FONT = "font"
    private const val COLOR_SCHEME = "color-scheme"
    private const val WORD_BASED_IME = "word-based-ime"

    fun create(): ShellProfile {
      return ShellProfile()
    }
  }

  override val profileMetaName = PROFILE_META_NAME

  var loginShell = DefaultValues.loginShell
  var initialCommand = DefaultValues.initialCommand

  var enableBell = DefaultValues.enableBell
  var enableVibrate = DefaultValues.enableVibrate
  var enableExecveWrapper = DefaultValues.enableExecveWrapper
  var enableSpecialVolumeKeys = DefaultValues.enableSpecialVolumeKeys
  var enableAutoCompletion = DefaultValues.enableAutoCompletion
  var enableBackKeyToEscape = DefaultValues.enableBackButtonBeMappedToEscape
  var enableExtraKeys = DefaultValues.enableExtraKeys
  var enableWordBasedIme = DefaultValues.enableWordBasedIme

  var profileFont: String
  var profileColorScheme: String

  init {
    val fontComp = ComponentManager.getComponent<FontComponent>()
    val colorComp = ComponentManager.getComponent<ColorSchemeComponent>()

    profileFont = fontComp.getCurrentFontName()
    profileColorScheme = colorComp.getCurrentColorSchemeName()

    loginShell = NeoPreference.getLoginShellPath()
    initialCommand = NeoPreference.getInitialCommand()
    enableBell = NeoPreference.isBellEnabled()
    enableVibrate = NeoPreference.isVibrateEnabled()
    enableExecveWrapper = NeoPreference.isExecveWrapperEnabled()
    enableSpecialVolumeKeys = NeoPreference.isSpecialVolumeKeysEnabled()
    enableAutoCompletion = NeoPreference.isAutoCompletionEnabled()
    enableBackKeyToEscape = NeoPreference.isBackButtonBeMappedToEscapeEnabled()
    enableExtraKeys = NeoPreference.isExtraKeysEnabled()
    enableWordBasedIme = NeoPreference.isWordBasedImeEnabled()
  }

  override fun onConfigLoaded(configVisitor: ConfigVisitor) {
    super.onConfigLoaded(configVisitor)
    loginShell = configVisitor.getProfileString(LOGIN_SHELL, loginShell)
    initialCommand = configVisitor.getProfileString(INITIAL_COMMAND, initialCommand)
    enableBell = configVisitor.getProfileBoolean(BELL, enableBell)
    enableVibrate = configVisitor.getProfileBoolean(VIBRATE, enableVibrate)
    enableExecveWrapper = configVisitor.getProfileBoolean(EXECVE_WRAPPER, enableExecveWrapper)
    enableSpecialVolumeKeys = configVisitor.getProfileBoolean(SPECIAL_VOLUME_KEYS, enableSpecialVolumeKeys)
    enableAutoCompletion = configVisitor.getProfileBoolean(AUTO_COMPLETION, enableAutoCompletion)
    enableBackKeyToEscape = configVisitor.getProfileBoolean(BACK_KEY_TO_ESC, enableBackKeyToEscape)
    enableExtraKeys = configVisitor.getProfileBoolean(EXTRA_KEYS, enableExtraKeys)
    enableWordBasedIme = configVisitor.getProfileBoolean(WORD_BASED_IME, enableWordBasedIme)
    profileFont = configVisitor.getProfileString(FONT, profileFont)
    profileColorScheme = configVisitor.getProfileString(COLOR_SCHEME, profileColorScheme)
  }
}