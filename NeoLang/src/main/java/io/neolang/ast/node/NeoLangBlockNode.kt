package io.neolang.ast.node

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
class NeoLangBlockNode(blockElement: NeoLangAstBaseNode) : NeoLangAstBasedNode(blockElement) {
    companion object {
        fun emptyNode() : NeoLangBlockNode {
            return NeoLangBlockNode(NeoLangDummyNode())
        }
    }
}