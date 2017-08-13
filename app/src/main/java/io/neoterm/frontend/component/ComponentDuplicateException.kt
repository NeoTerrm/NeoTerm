package io.neoterm.frontend.component

/**
 * @author kiva
 */
class ComponentDuplicateException(serviceName: String) : RuntimeException("Service $serviceName duplicate")