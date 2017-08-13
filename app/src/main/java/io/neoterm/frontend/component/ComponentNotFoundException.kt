package io.neoterm.frontend.component

/**
 * @author kiva
 */
class ComponentNotFoundException(serviceName: String) : RuntimeException("Service `$serviceName' not found") {
}