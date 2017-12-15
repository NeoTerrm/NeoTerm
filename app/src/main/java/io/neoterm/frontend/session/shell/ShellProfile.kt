package io.neoterm.frontend.session.shell

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.color.ColorSchemeComponent
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

        private const val LOGIN_SHELL_NAME = "login-shell"
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
    }

    override val profileMetaName = PROFILE_META_NAME

    var loginShell = DefaultValues.loginShell
    var initialCommand = DefaultValues.initialCommand

    var enableBell = DefaultValues.enableBell
    var enableVibrate = DefaultValues.enableVibrate
    var enableExecveWrapper = DefaultValues.enableExecveWrapper
    var enableSpecialVolumeKeys = DefaultValues.enableSpecialVolumeKeys
    var enableAutoCompletion = DefaultValues.enableAutoCompletion
    var enableBackButtonBeMappedToEscape = DefaultValues.enableBackButtonBeMappedToEscape
    var enableExtraKeys = DefaultValues.enableExtraKeys

    var profileFont: String
    var profileColorScheme: String

    init {
//        val fontComp = ComponentManager.getComponent<FontComponent>()
//        val colorComp = ComponentManager.getComponent<ColorSchemeComponent>()
//
//        profileFont = fontComp.getCurrentFontName()
//        profileColorScheme = colorComp.getCurrentColorSchemeName()

        profileFont = ""
        profileColorScheme = ""

//        loginShell = NeoPreference.getLoginShellPath()
//        initialCommand = NeoPreference.getInitialCommand()
//        enableBell = NeoPreference.isBellEnabled()
//        enableVibrate = NeoPreference.isVibrateEnabled()
//        enableExecveWrapper = NeoPreference.isExecveWrapperEnabled()
//        enableSpecialVolumeKeys = NeoPreference.isSpecialVolumeKeysEnabled()
//        enableAutoCompletion = NeoPreference.isAutoCompletionEnabled()
//        enableBackButtonBeMappedToEscape = NeoPreference.isBackButtonBeMappedToEscapeEnabled()
//        enableExtraKeys = NeoPreference.isExtraKeysEnabled()
    }

    override fun onProfileLoaded(visitor: ConfigVisitor): Boolean {
        return true
    }
}