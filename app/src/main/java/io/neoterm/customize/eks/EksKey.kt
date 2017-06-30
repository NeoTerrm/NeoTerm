package io.neoterm.customize.eks

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
interface EksKey {
    fun applyShortcutKeys(extraKeysView: ExtraKeysView)
}
