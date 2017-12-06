package io.neoterm.component.profile

import io.neoterm.frontend.component.NeoComponent

/**
 * @author kiva
 */
class ProfileComponent : NeoComponent {
    private val profileList = mutableListOf<Profile>()

    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }
}