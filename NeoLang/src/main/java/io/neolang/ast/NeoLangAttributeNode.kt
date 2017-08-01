package io.neolang.ast

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
class NeoLangAttributeNode(private val stringNode: NeoLangStringNode, private val blockNode: NeoLangBlockNode) : NeoLangAstBaseNode() {

    override fun toString(): String {
        return "NeoLangAttributeNode { stringNode: $stringNode, block: $blockNode }"
    }
}
