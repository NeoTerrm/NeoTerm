package io.neolang.ast.node

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
open class NeoLangAstBasedNode(val ast: NeoLangAstBaseNode) : NeoLangAstBaseNode() {
    override fun toString(): String {
        return "${javaClass.simpleName} { ast: $ast }"
    }
}