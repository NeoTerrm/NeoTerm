package io.neolang.ast

import io.neolang.ast.base.NeoLangAstBaseNode
import io.neolang.ast.typed.NeoLangAstTypedNode

/**
 * @author kiva
 */
class NeoLangBlockNode(ast: NeoLangAstBaseNode) : NeoLangAstTypedNode(ast) {
    companion object {
        fun emptyNode() :NeoLangBlockNode {
            return NeoLangBlockNode(NeoLangDummyNode())
        }
    }
}