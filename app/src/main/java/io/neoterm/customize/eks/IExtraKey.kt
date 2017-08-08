package io.neoterm.customize.eks

import io.neoterm.view.eks.ExtraKeysView

/**
 * @author kiva
 */
interface IExtraKey {
    fun applyShortcutKeys(extraKeysView: ExtraKeysView)
}
