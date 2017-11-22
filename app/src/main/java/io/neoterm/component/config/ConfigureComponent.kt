package com.termux.component.config

import com.termux.component.config.loader.NeoLangConfigureLoader
import com.termux.component.config.loader.OldConfigureLoader
import com.termux.frontend.component.NeoComponent
import java.io.File

/**
 * @author Sam
 */
class ConfigureComponent : NeoComponent {
    val CONFIG_LOADER_VERSION = 20

    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }

    fun getLoaderVersion(): Int {
        return CONFIG_LOADER_VERSION
    }

    fun newLoader(configFile: File): IConfigureFileLoader {
        return when (configFile.extension) {
            "nl" -> NeoLangConfigureLoader(configFile)
            else -> OldConfigureLoader(configFile)
        }
    }
}
