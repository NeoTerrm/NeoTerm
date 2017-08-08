package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */
class NeoLangAttributeNode(val stringNode: NeoLangStringNode, val blockNode: NeoLangBlockNode) : NeoLangBaseNode() {

    override fun toString(): String {
        return "NeoLangAttributeNode { stringNode: $stringNode, block: $blockNode }"
    }
}
