package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */
class NeoLangBlockNode(blockElement: NeoLangBaseNode) : NeoLangAstBasedNode(blockElement) {
    companion object {
        fun emptyNode(): NeoLangBlockNode {
            return NeoLangBlockNode(NeoLangDummyNode())
        }
    }
}