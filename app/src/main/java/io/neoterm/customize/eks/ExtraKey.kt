package io.neoterm.customize.eks

import io.neoterm.view.eks.ExtraKeysView

/**
 * @author kiva
 */
interface ExtraKey {
    fun applyShortcutKeys(extraKeysView: ExtraKeysView)
}
