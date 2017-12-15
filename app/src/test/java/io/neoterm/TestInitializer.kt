package io.neoterm

import io.neoterm.component.NeoInitializer
import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.component.completion.CompletionComponent
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.component.extrakey.ExtraKeysComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.component.pm.PackageComponent
import io.neoterm.component.script.UserScriptComponent
import io.neoterm.frontend.component.ComponentManager

/**
 * @author kiva
 */
object TestInitializer {
    fun init() {
        NeoInitializer.initComponents()
    }
}