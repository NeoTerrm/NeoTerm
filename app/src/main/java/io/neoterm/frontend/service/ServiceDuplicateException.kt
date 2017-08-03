package io.neoterm.frontend.service

/**
 * @author kiva
 */
class ServiceDuplicateException(serviceName: String) : RuntimeException("Service $serviceName duplicate")