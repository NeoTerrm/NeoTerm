package com.termux.component

import android.content.Context
import com.termux..component.codegen.CodeGenComponent
import com.termux.component.color.ColorSchemeComponent
import com.termux.component.completion.CompletionComponent
import com.termux.component.config.ConfigureComponent
import com.termux.component.eks.ExtraKeysComponent
import com.termux.component.font.FontComponent
import com.termux.component.pm.PackageComponent
import com.termux.component.script.UserScriptComponent
import com.termux.frontend.logging.NLog
import com.termux.frontend.component.ComponentManager

/**
 * @author Sam
 */
object NeoInitializer {
    fun init(context: Context) {
        NLog.init(context)
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
