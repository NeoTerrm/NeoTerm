package io.neoterm.frontend.component

import java.util.concurrent.ConcurrentHashMap

/**
 * @author kiva
 */
object ComponentManager {
    val THROW_WHEN_SERVICE_NOT_FOUND = true
    val SERVICE_CACHE = ConcurrentHashMap<Class<out NeoComponent>, NeoComponent>()

    fun registerComponent(serviceClass: Class<out NeoComponent>) {
        if (SERVICE_CACHE.containsKey(serviceClass)) {
            throw ComponentDuplicateException(serviceClass.simpleName)
        }
        val service = createServiceInstance(serviceClass)
        SERVICE_CACHE.put(serviceClass, service)
        service.onServiceInit()
    }

    fun unregisterComponent(serviceInterface: Class<out NeoComponent>) {
        val service = SERVICE_CACHE[serviceInterface]
        if (service != null) {
            service.onServiceDestroy()
            SERVICE_CACHE.remove(serviceInterface)
        }
    }

    inline fun <reified T : NeoComponent> getService(): T {
        val serviceInterface = T::class.java
        val service: NeoComponent? = SERVICE_CACHE[serviceInterface]

        if (service == null && THROW_WHEN_SERVICE_NOT_FOUND) {
            throw ComponentNotFoundException(serviceInterface.simpleName)
        }
        service?.onServiceObtained()
        return service as T
    }

    private fun createServiceInstance(serviceInterface: Class<out NeoComponent>): NeoComponent {
        return serviceInterface.newInstance()
    }
}
