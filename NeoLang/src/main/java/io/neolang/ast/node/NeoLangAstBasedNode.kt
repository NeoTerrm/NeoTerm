package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */
open class NeoLangAstBasedNode(val ast: NeoLangBaseNode) : NeoLangBaseNode() {
    override fun toString(): String {
        return "${javaClass.simpleName} { ast: $ast }"
    }
}