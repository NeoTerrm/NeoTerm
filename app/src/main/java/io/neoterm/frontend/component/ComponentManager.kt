package io.neoterm.frontend.component

import java.util.concurrent.ConcurrentHashMap

/**
 * @author kiva
 */
object ComponentManager {
    val COMPONENTS = ConcurrentHashMap<Class<out NeoComponent>, NeoComponent>()

    fun registerComponent(serviceClass: Class<out NeoComponent>) {
        if (COMPONENTS.containsKey(serviceClass)) {
            throw ComponentDuplicateException(serviceClass.simpleName)
        }
        val service = createServiceInstance(serviceClass)
        COMPONENTS.put(serviceClass, service)
        service.onServiceInit()
    }

    fun unregisterComponent(serviceInterface: Class<out NeoComponent>) {
        val service = COMPONENTS[serviceInterface]
        if (service != null) {
            service.onServiceDestroy()
            COMPONENTS.remove(serviceInterface)
        }
    }

    inline fun <reified T : NeoComponent> getComponent(): T {
        val serviceInterface = T::class.java
        val service: NeoComponent = COMPONENTS[serviceInterface] ?:
                throw ComponentNotFoundException(serviceInterface.simpleName)

        service.onServiceObtained()
        return service as T
    }

    private fun createServiceInstance(serviceInterface: Class<out NeoComponent>): NeoComponent {
        return serviceInterface.newInstance()
    }
}
