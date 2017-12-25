package io.neoterm.frontend.component.helper

import io.neolang.visitor.ConfigVisitor

/**
 * @author kiva
 */
interface ConfigFileBasedObject {
    @Throws(RuntimeException::class)
    fun onConfigLoaded(configVisitor: ConfigVisitor)
}