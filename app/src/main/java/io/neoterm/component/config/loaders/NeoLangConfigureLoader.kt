package io.neoterm.component.config.loaders

import io.neoterm.component.config.IConfigureLoader
import io.neoterm.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author kiva
 */
class NeoLangConfigureLoader(private val configFile: File) : IConfigureLoader {
    override fun loadConfigure(): NeoConfigureFile? {
        val configureFile = NeoConfigureFile(configFile)
        return if (configureFile.parseConfigure()) configureFile else null
    }
}