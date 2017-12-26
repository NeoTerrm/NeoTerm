package io.neoterm.component.profile

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.component.helper.ConfigFileBasedComponent
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
import org.jetbrains.annotations.TestOnly
import java.io.File

/**
 * @author kiva
 */
class ProfileComponent : ConfigFileBasedComponent<NeoProfile>() {
    override fun onCheckComponentFiles() {
        val profileDir = File(NeoTermPath.PROFILE_PATH)
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }
    }

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

    private val profileRegistry = mutableMapOf<String, Class<out NeoProfile>>()
    private val profileList = mutableListOf<NeoProfile>()

    fun registerProfile(metaName: String, prototype: Class<out NeoProfile>) {
        profileRegistry[metaName] = prototype
    }

    fun unregisterProfile(metaName: String) {
        profileRegistry.remove(metaName)
    }
}