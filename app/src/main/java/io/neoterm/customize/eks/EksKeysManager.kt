package io.neoterm.customize.eks

import android.content.Context
import io.neoterm.customize.eks.builtin.BuiltinEksKeys
import io.neoterm.view.eks.ExtraKeysView

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

        extraKeysView.loadDefaultUserKeys()
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

    fun init(context: Context) {
        BuiltinEksKeys.registerAll()
        EksConfigLoader.loadDefinedConfigs()
    }
}