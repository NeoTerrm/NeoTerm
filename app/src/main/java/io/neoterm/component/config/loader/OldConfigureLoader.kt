package com.termux.component.config.loader

import com.termux.component.config.IConfigureFileLoader
import com.termux.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author Sam
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
