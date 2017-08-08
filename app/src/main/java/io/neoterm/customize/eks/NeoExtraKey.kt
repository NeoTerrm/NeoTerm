package io.neoterm.customize.eks

import io.neoterm.view.eks.button.IExtraButton

/**
 * @author kiva
 */
class NeoExtraKey {
    var version: Int = -1
    val programNames: MutableList<String> = mutableListOf()
    val shortcutKeys: MutableList<IExtraButton> = mutableListOf()
    var withDefaultKeys: Boolean = true
}