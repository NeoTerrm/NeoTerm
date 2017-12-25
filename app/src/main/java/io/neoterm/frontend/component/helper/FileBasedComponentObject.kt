package io.neoterm.frontend.component.helper

import io.neolang.visitor.ConfigVisitor

/**
 * @author kiva
 */
interface FileBasedComponentObject {
    @Throws(RuntimeException::class)
    fun onConfigLoaded(configVisitor: ConfigVisitor)
}