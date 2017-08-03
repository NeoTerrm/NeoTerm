package io.neolang.ast.node

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
class NeoLangAttributeNode(val stringNode: NeoLangStringNode, val blockNode: NeoLangBlockNode) : NeoLangAstBaseNode() {

    override fun toString(): String {
        return "NeoLangAttributeNode { stringNode: $stringNode, block: $blockNode }"
    }
}
