package io.neoterm.customize.shortcut

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
interface ShortcutKey {
    fun applyShortcutKeys(extraKeysView: ExtraKeysView)
}
