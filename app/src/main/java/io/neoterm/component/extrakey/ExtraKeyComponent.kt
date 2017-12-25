package io.neoterm.component.extrakey

import android.content.Context
import io.neoterm.App
import io.neoterm.frontend.component.helper.ConfigFileBasedComponent
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
import io.neoterm.utils.AssetsUtils
import java.io.File
import java.io.FileFilter

/**
 * @author kiva
 */
class ExtraKeyComponent : ConfigFileBasedComponent<NeoExtraKey>() {
    companion object {
        private val FILTER = FileFilter {
            it.extension == "nl"
        }
    }

    override val checkComponentFileWhenObtained = true

    private val extraKeys: MutableMap<String, NeoExtraKey> = mutableMapOf()

    override fun onCheckComponentFiles() {
        File(NeoTermPath.EKS_PATH).mkdirs()

        val defaultFile = File(NeoTermPath.EKS_DEFAULT_FILE)
        if (!defaultFile.exists()) {
            extractDefaultConfig(App.get())
        }
        reloadExtraKeyConfig()
    }

    override fun onCreateComponentObject(): NeoExtraKey {
        return NeoExtraKey()
    }

    fun showShortcutKeys(program: String, extraKeysView: ExtraKeysView?) {
        if (extraKeysView == null) {
            return
        }

        val extraKey = extraKeys[program]
        if (extraKey != null) {
            extraKey.applyExtraKeys(extraKeysView)
            return
        }

        extraKeysView.loadDefaultUserKeys()
    }

    private fun registerShortcutKeys(extraKey: NeoExtraKey) {
        extraKey.programNames.forEach {
            extraKeys[it] = extraKey
        }
    }

    private fun extractDefaultConfig(context: Context) {
        try {
            AssetsUtils.extractAssetsDir(context, "eks", NeoTermPath.EKS_PATH)
        } catch (e: Exception) {
            NLog.e("ExtraKey", "Failed to extract configure: ${e.localizedMessage}")
        }
    }

    private fun reloadExtraKeyConfig() {
        val configDir = File(NeoTermPath.EKS_PATH)

        configDir.listFiles(FILTER).forEach {
            if (it.absolutePath != NeoTermPath.EKS_DEFAULT_FILE) {
                val extraKey = this.loadConfigure(it)
                if (extraKey != null) {
                    registerShortcutKeys(extraKey)
                }
            }
        }
    }
}