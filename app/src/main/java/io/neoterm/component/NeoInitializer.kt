package io.neoterm.component

import android.content.Context
import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.component.completion.CompletionComponent
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.component.extrakey.ExtraKeysComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.component.pm.PackageComponent
import io.neoterm.component.profile.ProfileComponent
import io.neoterm.component.script.UserScriptComponent
import io.neoterm.component.session.SessionComponent
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.session.shell.ShellProfile

/**
 * @author kiva
 */
object NeoInitializer {
    fun init(context: Context) {
        NLog.init(context)
        initComponents()
    }

    fun initComponents() {
        ComponentManager.registerComponent(ConfigureComponent::class.java)
        ComponentManager.registerComponent(CodeGenComponent::class.java)
        ComponentManager.registerComponent(ColorSchemeComponent::class.java)
        ComponentManager.registerComponent(FontComponent::class.java)
        ComponentManager.registerComponent(UserScriptComponent::class.java)
        ComponentManager.registerComponent(ExtraKeysComponent::class.java)
        ComponentManager.registerComponent(CompletionComponent::class.java)
        ComponentManager.registerComponent(PackageComponent::class.java)
        ComponentManager.registerComponent(SessionComponent::class.java)
        ComponentManager.registerComponent(ProfileComponent::class.java)

        val profileComp = ComponentManager.getComponent<ProfileComponent>()
        profileComp.registerProfile(ShellProfile.PROFILE_META_NAME, ShellProfile::class.java)
    }
}