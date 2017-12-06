package io.neoterm.component.profile

import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.session.shell.ShellProfile

/**
 * @author kiva
 */
class ProfileComponent : NeoComponent {
    private val profileList = mutableListOf<ShellProfile>()

    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }


}