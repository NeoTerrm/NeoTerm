package io.neoterm.customize.eks

import io.neoterm.customize.eks.builtin.BuiltinExtraKeys
import io.neoterm.frontend.service.NeoService
import io.neoterm.view.eks.ExtraKeysView

/**
 * @author kiva
 */
class ExtraKeysService : NeoService {
    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }

    val EKS_KEYS: MutableMap<String, IExtraKey> = mutableMapOf()

    fun showShortcutKeys(program: String, extraKeysView: ExtraKeysView?) {
        if (extraKeysView == null) {
            return
        }

        if (this.EKS_KEYS.containsKey(program)) {
            val shortcutKey = EKS_KEYS[program]
            shortcutKey?.applyShortcutKeys(extraKeysView)
            return
        }

        extraKeysView.loadDefaultUserKeys()
    }

    fun registerShortcutKeys(program: String, eksKey: IExtraKey?) {
        if (eksKey == null) {
            if (this.EKS_KEYS.containsKey(program)) {
                this.EKS_KEYS.remove(program)
            }
            return
        }

        this.EKS_KEYS[program] = eksKey
    }

    private fun checkForFiles() {
        BuiltinExtraKeys.registerAll()
        ExtraKeyConfigLoader.loadDefinedConfigs(this)
    }
}