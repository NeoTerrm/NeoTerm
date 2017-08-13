package io.neoterm.component.eks

import android.content.Context
import io.neoterm.App
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.utils.AssetsUtils
import io.neoterm.frontend.terminal.eks.ExtraKeysView
import java.io.File
import java.io.FileFilter

/**
 * @author kiva
 */
class ExtraKeysComponent : NeoComponent {
    companion object {
        private val FILTER = FileFilter {
            it.extension == "nl"
        }
    }
    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }

    val extraKeys: MutableMap<String, NeoExtraKey> = mutableMapOf()

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

    fun registerShortcutKeys(extraKey: NeoExtraKey) {
        extraKey.programNames.forEach {
            extraKeys[it] = extraKey
        }
    }

    private fun checkForFiles() {
        File(NeoTermPath.EKS_PATH).mkdirs()

        val defaultFile = File(NeoTermPath.EKS_DEFAULT_FILE)
        if (!defaultFile.exists()) {
            extractDefaultConfig(App.get())
        }
        loadConfigure()
    }

    private fun extractDefaultConfig(context: Context) {
        try {
            AssetsUtils.extractAssetsDir(context, "eks", NeoTermPath.EKS_PATH)
        } catch (e: Exception) {
            NLog.e("ExtraKey", "Failed to extract configure: ${e.localizedMessage}")
        }
    }

    private fun loadConfigure() {
        val configDir = File(NeoTermPath.EKS_PATH)

        configDir.listFiles(FILTER).forEach {
            if (it.absolutePath != NeoTermPath.EKS_DEFAULT_FILE) {
                val extraKey = NeoExtraKey()
                if (extraKey.loadConfigure(it)) {
                    registerShortcutKeys(extraKey)
                }
            }
        }
    }
}