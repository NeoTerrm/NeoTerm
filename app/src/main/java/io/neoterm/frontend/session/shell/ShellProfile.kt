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

        private val PROFILE_META_PATH = arrayOf(PROFILE_META_NAME)
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
    }

    override fun onProfileLoaded(V: ConfigVisitor): Boolean {
        loginShell = V.getProfileString(LOGIN_SHELL, loginShell)
        initialCommand = V.getProfileString(INITIAL_COMMAND, initialCommand)
        enableBell = V.getProfileBoolean(BELL, enableBell)
        enableVibrate = V.getProfileBoolean(VIBRATE, enableVibrate)
        enableExecveWrapper = V.getProfileBoolean(EXECVE_WRAPPER, enableExecveWrapper)
        enableSpecialVolumeKeys = V.getProfileBoolean(SPECIAL_VOLUME_KEYS, enableSpecialVolumeKeys)
        enableAutoCompletion = V.getProfileBoolean(AUTO_COMPLETION, enableAutoCompletion)
        enableBackKeyToEscape = V.getProfileBoolean(BACK_KEY_TO_ESC, enableBackKeyToEscape)
        enableExtraKeys = V.getProfileBoolean(EXTRA_KEYS, enableExtraKeys)
        profileFont = V.getProfileString(FONT, profileFont)
        profileColorScheme = V.getProfileString(COLOR_SCHEME, profileColorScheme)
        return true
    }

    private fun ConfigVisitor.getProfileString(key: String, fallback: String): String {
        return getProfileString(key) ?: fallback
    }

    private fun ConfigVisitor.getProfileBoolean(key: String, fallback: Boolean): Boolean {
        return getProfileBoolean(key) ?: fallback
    }

    private fun ConfigVisitor.getProfileString(key: String): String? {
        return this.getStringValue(PROFILE_META_PATH, key)
    }

    private fun ConfigVisitor.getProfileBoolean(key: String): Boolean? {
        return this.getBooleanValue(PROFILE_META_PATH, key)
    }
}