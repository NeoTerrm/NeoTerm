package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */
class NeoLangArrayNode(val arrayNameNode: NeoLangStringNode, val elements: Array<ArrayElement>) : NeoLangBaseNode() {
    companion object {
        class ArrayElement(val index: Int, val block: NeoLangBlockNode)
    }
}