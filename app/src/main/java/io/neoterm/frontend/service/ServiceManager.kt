package io.neoterm.frontend.service

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * @author kiva
 */
object ServiceManager {
    val THROW_WHEN_SERVICE_NOT_FOUND = true
    val SERVICE_CACHE = ConcurrentHashMap<Class<out NeoService>, NeoService>()

    fun registerService(serviceInterface: Class<out NeoService>) {
        val service = createServiceInstance(serviceInterface)
        SERVICE_CACHE.put(serviceInterface, service)
        service.onServiceInit()
    }

    fun unregisterService(serviceInterface: Class<out NeoService>) {
        val service = SERVICE_CACHE[serviceInterface]
        if (service != null) {
            service.onServiceDestroy()
            SERVICE_CACHE.remove(serviceInterface)
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : NeoService> getService(): T {
        Log.e("NeoTerm", SERVICE_CACHE.keys.toString())
        val serviceInterface = T::class.java
        val service: NeoService? = SERVICE_CACHE[serviceInterface]

        if (service == null && THROW_WHEN_SERVICE_NOT_FOUND) {
            throw ServiceNotFoundException(serviceInterface.simpleName)
        }
        service?.onServiceObtained()
        return service as T
    }

    private fun createServiceInstance(serviceInterface: Class<out NeoService>): NeoService {
        return serviceInterface.newInstance()
    }
}
