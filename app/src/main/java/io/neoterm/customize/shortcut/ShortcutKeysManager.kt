package io.neoterm.customize.shortcut

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
object ShortcutKeysManager {
    val shortcutKeys: MutableMap<String, ShortcutKey> = mutableMapOf()

    fun showShortcutKeys(program: String, extraKeysView: ExtraKeysView?) {
        if (extraKeysView == null) {
            return
        }

        if (this.shortcutKeys.containsKey(program)) {
            val shortcutKey = shortcutKeys[program]
            shortcutKey?.applyShortcutKeys(extraKeysView)
            return
        }

        extraKeysView.loadDefaultUserDefinedExtraKeys()
    }

    fun registerShortcutKeys(program: String, shortcutKey: ShortcutKey?) {
        if (shortcutKey == null) {
            if (this.shortcutKeys.containsKey(program)) {
                this.shortcutKeys.remove(program)
            }
            return
        }

        this.shortcutKeys[program] = shortcutKey
    }
}