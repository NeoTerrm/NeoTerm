package io.neoterm.frontend.component

import java.util.concurrent.ConcurrentHashMap

/**
 * @author kiva
 */
object ComponentManager {
    private val COMPONENTS = ConcurrentHashMap<Class<out NeoComponent>, NeoComponent>()

    fun registerComponent(componentClass: Class<out NeoComponent>) {
        if (COMPONENTS.containsKey(componentClass)) {
            throw ComponentDuplicateException(componentClass.simpleName)
        }
        val service = createServiceInstance(componentClass)
        COMPONENTS.put(componentClass, service)
        service.onServiceInit()
    }

    fun unregisterComponent(componentInterface: Class<out NeoComponent>) {
        val service = COMPONENTS[componentInterface]
        if (service != null) {
            service.onServiceDestroy()
            COMPONENTS.remove(componentInterface)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : NeoComponent> getComponent(componentInterface: Class<T>) : T {
        val service: NeoComponent = COMPONENTS[componentInterface] ?:
                throw ComponentNotFoundException(componentInterface.simpleName)

        service.onServiceObtained()
        return service as T
    }

    inline fun <reified T : NeoComponent> getComponent(): T {
        val serviceInterface = T::class.java
        return getComponent(serviceInterface);
    }

    private fun createServiceInstance(serviceInterface: Class<out NeoComponent>): NeoComponent {
        return serviceInterface.newInstance()
    }
}
