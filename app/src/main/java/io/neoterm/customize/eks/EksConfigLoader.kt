package io.neoterm.customize.eks

import android.util.Log
import io.neoterm.preference.NeoTermPath
import io.neoterm.view.eks.ExtraKeysView
import java.io.File

/**
 * @author kiva
 */
object EksConfigLoader {
    class ConfiguredEksKey(val config: EksConfig) : EksKey {
        override fun applyShortcutKeys(extraKeysView: ExtraKeysView) {
            if (config.withDefaultKeys) {
                extraKeysView.loadDefaultUserKeys()
            }
            for (button in config.shortcutKeys) {
                extraKeysView.addUserKey(button)
            }
        }
    }

    fun loadDefinedConfigs() {
        val configDir = File(NeoTermPath.EKS_PATH)
        configDir.mkdirs()

        val parser = EksConfigParser()
        for (file in configDir.listFiles()) {
            try {
                parser.setInput(file)
                val config = parser.parse()

                // "default" is a reserved program used for default extra keys
                // see ExtraKeysView.loadDefaultUserKeys()
                if (config.programNames.contains("default")) {
                    continue
                }
                registerConfig(config)
            } catch (e: Exception) {
                Log.e("NeoTerm-EKS", "Load $file failed: " + e.toString())
            }
        }
    }

    private fun registerConfig(config: EksConfig) {
        val shortcutKey = ConfiguredEksKey(config)
        for (programName in config.programNames) {
            EksKeysManager.registerShortcutKeys(programName, shortcutKey)
        }
    }
}