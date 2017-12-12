package io.neoterm.frontend.session.shell

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
        enableBackButtonBeMappedToEscape = NeoPreference.isBackButtonBeMappedToEscapeEnabled()
        enableExtraKeys = NeoPreference.isExtraKeysEnabled()
    }
}