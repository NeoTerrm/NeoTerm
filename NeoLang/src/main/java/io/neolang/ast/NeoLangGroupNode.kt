package io.neolang.ast

import io.neolang.ast.base.NeoLangAstBaseNode

/**
 * @author kiva
 */
class NeoLangGroupNode(private val attributes: List<NeoLangAttributeNode>) : NeoLangAstBaseNode() {

    override fun toString(): String {
        return "NeoLangGroupNode { attrs: $attributes }"
    }
}