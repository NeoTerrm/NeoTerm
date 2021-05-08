package io.neoterm.component

import android.content.Context
import io.neoterm.component.codegen.CodeGenComponent
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.completion.CompletionComponent
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.component.extrakey.ExtraKeyComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.component.pm.PackageComponent
import io.neoterm.component.profile.ProfileComponent
import io.neoterm.component.session.SessionComponent
import io.neoterm.component.session.ShellProfile
import io.neoterm.component.userscript.UserScriptComponent
import io.neoterm.utils.NLog
import java.util.concurrent.ConcurrentHashMap

interface NeoComponent {
  fun onServiceInit()
  fun onServiceDestroy()
  fun onServiceObtained()
}

object ComponentManager {
  private val COMPONENTS = ConcurrentHashMap<Class<out NeoComponent>, NeoComponent>()

  fun registerComponent(componentClass: Class<out NeoComponent>) {
    if (COMPONENTS.containsKey(componentClass)) {
      throw ComponentDuplicateException(componentClass.simpleName)
    }
    val component = createServiceInstance(componentClass)
    COMPONENTS.put(componentClass, component)
    component.onServiceInit()
  }

  fun unregisterComponent(componentInterface: Class<out NeoComponent>) {
    val component = COMPONENTS[componentInterface]
    if (component != null) {
      component.onServiceDestroy()
      COMPONENTS.remove(componentInterface)
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : NeoComponent> getComponent(componentInterface: Class<T>, errorThrow: Boolean = true): T {
    val component: NeoComponent =
      COMPONENTS[componentInterface] ?: throw ComponentNotFoundException(componentInterface.simpleName)

    component.onServiceObtained()
    return component as T
  }

  inline fun <reified T : NeoComponent> getComponent(): T {
    val componentInterface = T::class.java
    return getComponent(componentInterface);
  }

  private fun createServiceInstance(componentInterface: Class<out NeoComponent>): NeoComponent {
    return componentInterface.newInstance()
  }
}

class ComponentDuplicateException(serviceName: String) : RuntimeException("Service $serviceName duplicate")
class ComponentNotFoundException(serviceName: String) : RuntimeException("Component `$serviceName' not found")

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
    ComponentManager.registerComponent(ExtraKeyComponent::class.java)
    ComponentManager.registerComponent(CompletionComponent::class.java)
    ComponentManager.registerComponent(PackageComponent::class.java)
    ComponentManager.registerComponent(SessionComponent::class.java)
    ComponentManager.registerComponent(ProfileComponent::class.java)

    val profileComp = ComponentManager.getComponent<ProfileComponent>()
    profileComp.registerProfile(ShellProfile.PROFILE_META_NAME, ShellProfile::class.java)
  }
}
