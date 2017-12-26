package io.neoterm.component.profile

import io.neolang.visitor.ConfigVisitor
import io.neoterm.frontend.component.helper.ConfigFileBasedComponent
import io.neoterm.frontend.config.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
class ProfileComponent : ConfigFileBasedComponent<NeoProfile>(NeoTermPath.PROFILE_PATH) {
    override val checkComponentFileWhenObtained = true

    private val profileRegistry = mutableMapOf<String, Class<out NeoProfile>>()
    private val profileList = mutableListOf<NeoProfile>()

    override fun onCheckComponentFiles() = reloadProfiles()

    override fun onCreateComponentObject(configVisitor: ConfigVisitor): NeoProfile {
        val rootContext = configVisitor.getRootContext()

        val profileClass = rootContext.children
                .mapNotNull { profileRegistry[it.contextName] }
                .singleOrNull()

        if (profileClass != null) {
            return profileClass.newInstance()
        }

        throw IllegalArgumentException("No proper profile registry for found")
    }

    fun reloadProfiles() {
        profileList.clear()

    }

    fun registerProfile(metaName: String, prototype: Class<out NeoProfile>) {
        profileRegistry[metaName] = prototype
    }

    fun unregisterProfile(metaName: String) {
        profileRegistry.remove(metaName)
    }
}