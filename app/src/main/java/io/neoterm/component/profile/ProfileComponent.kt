package io.neoterm.component.profile

import io.neoterm.component.config.ConfigureComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
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

    inline fun <reified T : NeoProfile> loadConfigure(file: File): T {
        return loadConfigure(file, T::class.java) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun loadConfigure(file: File, clazz: Class<out NeoProfile>): NeoProfile {
        val loaderService = ComponentManager.getComponent<ConfigureComponent>()

        val configure: NeoConfigureFile?
        try {
            configure = loaderService.newLoader(file).loadConfigure()
            if (configure == null) {
                throw RuntimeException("Parse configuration failed.")
            }
        } catch (e: Exception) {
            NLog.e("Profile", "Failed to load profile: ${file.absolutePath}: ${e.localizedMessage}")
            throw e
        }

        val visitor = configure.getVisitor()
        val rootContext = visitor.getRootContext()

        val profileClass = rootContext.children
                .mapNotNull { profileRegistry[it.contextName] }
                .singleOrNull()

        if (profileClass != null) {
            val profile = profileClass.newInstance()
            profile.onProfileLoaded(visitor)
            return profile
        }

        throw IllegalArgumentException("No profile registry for ${clazz.simpleName} found")
    }

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }

    private fun checkForFiles() {
        val profileDir = File(NeoTermPath.PROFILE_PATH)
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }
    }
}