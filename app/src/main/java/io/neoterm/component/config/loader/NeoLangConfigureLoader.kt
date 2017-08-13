package io.neoterm.component.config.loader

import io.neoterm.component.config.IConfigureFileLoader
import io.neoterm.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author kiva
 */
class NeoLangConfigureLoader(val configFile: File) : IConfigureFileLoader {
    override fun loadConfigure(): NeoConfigureFile? {
        val configureFile = NeoConfigureFile(configFile)
        return if (configureFile.parseConfigure()) configureFile else null
    }
}