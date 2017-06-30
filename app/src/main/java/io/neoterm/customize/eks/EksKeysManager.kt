package io.neoterm.customize.eks

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
object EksKeysManager {
    val EKS_KEYS: MutableMap<String, EksKey> = mutableMapOf()

    fun showShortcutKeys(program: String, extraKeysView: ExtraKeysView?) {
        if (extraKeysView == null) {
            return
        }

        if (this.EKS_KEYS.containsKey(program)) {
            val shortcutKey = EKS_KEYS[program]
            shortcutKey?.applyShortcutKeys(extraKeysView)
            return
        }

        extraKeysView.loadDefaultUserDefinedExtraKeys()
    }

    fun registerShortcutKeys(program: String, eksKey: EksKey?) {
        if (eksKey == null) {
            if (this.EKS_KEYS.containsKey(program)) {
                this.EKS_KEYS.remove(program)
            }
            return
        }

        this.EKS_KEYS[program] = eksKey
    }
}