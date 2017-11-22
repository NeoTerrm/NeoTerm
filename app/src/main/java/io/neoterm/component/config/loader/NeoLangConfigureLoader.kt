package com.termux.component.config.loader

import com.termux.component.config.IConfigureFileLoader
import com.termux.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author Sam
 */
class NeoLangConfigureLoader(val configFile: File) : IConfigureFileLoader {
    override fun loadConfigure(): NeoConfigureFile? {
        val configureFile = NeoConfigureFile(configFile)
        return if (configureFile.parseConfigure()) configureFile else null
    }
}
