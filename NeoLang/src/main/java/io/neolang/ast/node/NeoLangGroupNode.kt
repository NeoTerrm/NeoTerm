package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */
class NeoLangGroupNode(val attributes: Array<NeoLangAttributeNode>) : NeoLangBaseNode() {

    override fun toString(): String {
        return "NeoLangGroupNode { attrs: $attributes }"
    }
}