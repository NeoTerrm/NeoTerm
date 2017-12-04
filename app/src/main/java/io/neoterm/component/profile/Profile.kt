package io.neoterm.component.profile

import io.neoterm.frontend.preference.DefaultPreference

/**
 * @author kiva
 */
class Profile {
    var profileShell = DefaultPreference.loginShell
    var profileInitialCommand = DefaultPreference.initialCommand

    var enableBell = DefaultPreference.enableBell
    var enableVibrate = DefaultPreference.enableVibrate
    var enableExecveWrapper = DefaultPreference.enableExecveWrapper
    var enableSpecialVolumeKeys = DefaultPreference.enableSpecialVolumeKeys

    lateinit var profileFont: String
    lateinit var profileColorScheme: String
}