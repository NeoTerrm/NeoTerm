package io.neoterm.component.profile

import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.session.shell.ShellProfile
import java.io.File

/**
 * @author kiva
 */
class ProfileComponent : NeoComponent {
    private val profileList = mutableListOf<ShellProfile>()

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