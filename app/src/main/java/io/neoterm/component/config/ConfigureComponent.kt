package io.neoterm.component.config

import io.neoterm.component.color.NeoColorScheme
import io.neoterm.component.config.export.ColorSchemeExporter
import io.neoterm.component.config.loader.NeoLangConfigureLoader
import io.neoterm.component.config.loader.OldConfigureLoader
import io.neoterm.frontend.component.NeoComponent
import java.io.File

/**
 * @author kiva
 */
class ConfigureComponent : NeoComponent {
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

    fun export(colorScheme: NeoColorScheme): String {
        return ColorSchemeExporter(colorScheme).getContent()
    }
}