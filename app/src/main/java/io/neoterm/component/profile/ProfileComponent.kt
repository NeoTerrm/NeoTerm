package io.neoterm.component.profile

import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.session.shell.ShellProfile
import java.io.File

/**
 * @author kiva
 */
class ProfileComponent : NeoComponent {
    private val profileRegistry = mutableMapOf<String, Class<out NeoProfile>>()
    private val profileList = mutableListOf<NeoProfile>()

    fun registerProfile(metaName: String, prototype: Class<out NeoProfile>) {
        profileRegistry[metaName] = prototype
    }

    fun unregisterProfile(metaName: String) {
        profileRegistry.remove(metaName)
    }

    private fun checkForFiles() {
        val profileDir = File(NeoTermPath.PROFILE_PATH)
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }
    }

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }
}