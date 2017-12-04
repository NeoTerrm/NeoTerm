package io.neoterm.component.profile

/**
 * @author kiva
 */
class Profile {
    lateinit var profileShell: String
    lateinit var profileFont: String
    lateinit var profileColorScheme: String
    lateinit var profileInitialCommand: String
    var enableBell = false
    var enableVibrate = false
    var enableExecveWrapper = true
    var specializeVolumeKeys = false
}