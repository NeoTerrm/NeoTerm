package io.neoterm

import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.component.completion.CompletionComponent
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.component.eks.ExtraKeysComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.component.pm.PackageComponent
import io.neoterm.component.script.UserScriptComponent
import io.neoterm.frontend.component.ComponentManager

/**
 * @author kiva
 */
object TestInitializer {
    fun init() {
        ComponentManager.registerComponent(ConfigureComponent::class.java)
        ComponentManager.registerComponent(CodeGenComponent::class.java)
        ComponentManager.registerComponent(ColorSchemeComponent::class.java)
        ComponentManager.registerComponent(FontComponent::class.java)
        ComponentManager.registerComponent(UserScriptComponent::class.java)
        ComponentManager.registerComponent(ExtraKeysComponent::class.java)
        ComponentManager.registerComponent(CompletionComponent::class.java)
        ComponentManager.registerComponent(PackageComponent::class.java)
    }
}