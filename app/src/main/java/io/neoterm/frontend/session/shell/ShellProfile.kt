package io.neoterm.frontend.session.shell

import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.preference.DefaultPreference
import io.neoterm.frontend.preference.NeoPreference

/**
 * @author kiva
 */
class ShellProfile {
    var loginShell = DefaultPreference.loginShell
    var initialCommand = DefaultPreference.initialCommand

    var enableBell = DefaultPreference.enableBell
    var enableVibrate = DefaultPreference.enableVibrate
    var enableExecveWrapper = DefaultPreference.enableExecveWrapper
    var enableSpecialVolumeKeys = DefaultPreference.enableSpecialVolumeKeys
    var enableExitMessage = DefaultPreference.enableExitMessage;

    var profileFont: String
    var profileColorScheme: String

    init {
        val fontComp = ComponentManager.getComponent<FontComponent>()
        val colorComp = ComponentManager.getComponent<ColorSchemeComponent>()

        profileFont = fontComp.getCurrentFontName()
        profileColorScheme = colorComp.getCurrentColorSchemeName()

        loginShell = NeoPreference.getLoginShellPath()
    }
}