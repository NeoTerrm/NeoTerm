package io.neolang.ast.typed

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
open class NeoLangAstTypedNode(val ast: NeoLangAstBaseNode) : NeoLangAstBaseNode() {
    override fun toString(): String {
        return "${javaClass.simpleName} { ast: $ast }"
    }
}