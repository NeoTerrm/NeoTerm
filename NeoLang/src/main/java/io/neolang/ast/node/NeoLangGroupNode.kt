package io.neolang.ast.node

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
class NeoLangGroupNode(val attributes: List<NeoLangAttributeNode>) : NeoLangAstBaseNode() {

    override fun toString(): String {
        return "NeoLangGroupNode { attrs: $attributes }"
    }
}