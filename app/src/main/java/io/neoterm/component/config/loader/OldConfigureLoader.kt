package io.neoterm.component.config.loader

import io.neoterm.component.config.IConfigureFileLoader
import io.neoterm.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author kiva
 */
class OldConfigureLoader(val configFile: File) : IConfigureFileLoader {
    override fun loadConfigure(): NeoConfigureFile? {
        return when (configFile.extension) {
            "eks" -> returnConfigure(OldExtraKeysConfigureFile(configFile))
            "color" -> returnConfigure(OldColorSchemeConfigureFile(configFile))
            else -> null
        }
    }

    private fun returnConfigure(configureFile: NeoConfigureFile): NeoConfigureFile? {
        return if (configureFile.parseConfigure()) configureFile else null
    }
}