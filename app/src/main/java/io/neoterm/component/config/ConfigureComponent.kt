package io.neoterm.component.config

import io.neoterm.component.config.loaders.NeoLangConfigureLoader
import io.neoterm.component.config.loaders.OldConfigureLoader
import io.neoterm.frontend.component.NeoComponent
import java.io.File

/**
 * @author kiva
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

  fun newLoader(configFile: File): IConfigureLoader {
    return when (configFile.extension) {
      "nl" -> NeoLangConfigureLoader(configFile)
      else -> OldConfigureLoader(configFile)
    }
  }
}