package io.neoterm.customize.config

import io.neoterm.customize.config.loader.NeoLangConfigureLoader
import io.neoterm.customize.config.loader.OldConfigureLoader
import io.neoterm.frontend.service.NeoService
import java.io.File

/**
 * @author kiva
 */
class ConfigureService : NeoService {
    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }

    fun newLoader(configFile: File): IConfigureFileLoader {
        return when (configFile.extension) {
            "nl" -> NeoLangConfigureLoader(configFile)
            else -> OldConfigureLoader(configFile)
        }
    }
}