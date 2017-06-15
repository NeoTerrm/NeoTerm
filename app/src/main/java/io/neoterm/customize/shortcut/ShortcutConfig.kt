package io.neoterm.customize.shortcut

import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
class ShortcutConfig {
    var version: Int = -1
    val programNames: MutableList<String> = mutableListOf()
    val shortcutKeys: MutableList<ExtraKeysView.ExtraButton> = mutableListOf()
}