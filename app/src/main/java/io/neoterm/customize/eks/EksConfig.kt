package io.neoterm.customize.eks

import io.neoterm.view.eks.ExtraButton

/**
 * @author kiva
 */
class EksConfig {
    var version: Int = -1
    val programNames: MutableList<String> = mutableListOf()
    val shortcutKeys: MutableList<ExtraButton> = mutableListOf()
    var withDefaultKeys: Boolean = true
}