package io.neoterm.component.config.loaders

import io.neoterm.component.config.IConfigureLoader
import io.neoterm.frontend.config.NeoConfigureFile
import java.io.File

/**
 * @author kiva
 */
class OldConfigureLoader(private val configFile: File) : IConfigureLoader {
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