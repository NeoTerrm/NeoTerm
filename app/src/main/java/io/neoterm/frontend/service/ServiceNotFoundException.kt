package io.neoterm.frontend.service

/**
 * @author kiva
 */
class ServiceNotFoundException(serviceName: String) : RuntimeException("Service `$serviceName' not found") {
}