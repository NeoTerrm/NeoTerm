package io.neoterm.customize.shortcut.builtin

import io.neoterm.customize.shortcut.ShortcutKey
import io.neoterm.customize.shortcut.ShortcutKeysManager
import io.neoterm.view.ExtraKeysView

/**
 * @author kiva
 */
object BuiltinShortcutKeys {
    val vim = object : ShortcutKey {
        override fun applyShortcutKeys(extraKeysView: ExtraKeysView) {
            extraKeysView.addExternalButton(ExtraKeysView.SLASH) // Search
            extraKeysView.addExternalButton(ExtraKeysView.TextButton(":w", true)) // Save
            extraKeysView.addExternalButton(ExtraKeysView.TextButton("dd", true)) // Delete
            extraKeysView.addExternalButton(ExtraKeysView.TextButton(":q", true)) // Quit
        }
    }

    val moreAndLess = object : ShortcutKey {
        override fun applyShortcutKeys(extraKeysView: ExtraKeysView) {
            extraKeysView.addExternalButton(ExtraKeysView.TextButton("R", true)) // Rest
            extraKeysView.addExternalButton(ExtraKeysView.TextButton("Q", true)) // Quit
        }
    }

    fun registerAll() {
        ShortcutKeysManager.registerShortcutKeys("vim", vim)
        ShortcutKeysManager.registerShortcutKeys("more", moreAndLess)
        ShortcutKeysManager.registerShortcutKeys("less", moreAndLess)
    }
}