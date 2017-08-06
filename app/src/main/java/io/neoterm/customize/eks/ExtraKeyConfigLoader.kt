package io.neoterm.customize.eks

import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.view.eks.ExtraKeysView
import java.io.File

/**
 * @author kiva
 */
object ExtraKeyConfigLoader {
    class ConfiguredExtraKey(val config: ExtraKeyConfig) : ExtraKey {
        override fun applyShortcutKeys(extraKeysView: ExtraKeysView) {
            if (config.withDefaultKeys) {
                extraKeysView.loadDefaultUserKeys()
            }
            for (button in config.shortcutKeys) {
                extraKeysView.addUserKey(button)
            }
        }
    }

    fun loadDefinedConfigs(extraKeysManager: ExtraKeysService) {
        val configDir = File(NeoTermPath.EKS_PATH)
        configDir.mkdirs()

        val parser = ExtraKeyConfigParser()
        for (file in configDir.listFiles()) {
            try {
                parser.setInput(file)
                val config = parser.parse()

                // "default" is a reserved program used for default extra keys
                // see ExtraKeysView.loadDefaultUserKeys()
                if (config.programNames.contains("default")) {
                    continue
                }
                registerConfig(extraKeysManager, config)
            } catch (e: Exception) {
                NLog.e("NeoTerm-EKS", "Load $file failed: " + e.toString())
            }
        }
    }

    private fun registerConfig(extraKeysManager: ExtraKeysService, config: ExtraKeyConfig) {
        val shortcutKey = ConfiguredExtraKey(config)
        for (programName in config.programNames) {
            extraKeysManager.registerShortcutKeys(programName, shortcutKey)
        }
    }
}