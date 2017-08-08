package io.neolang.ast.node

import io.neolang.ast.base.NeoLangBaseNode

/**
 * @author kiva
 */

class NeoLangProgramNode(val groups: List<NeoLangGroupNode>) : NeoLangBaseNode() {

    override fun toString(): String {
        return "NeoLangProgramNode { groups: $groups }"
    }

    companion object {
        fun emptyNode() : NeoLangProgramNode {
            return NeoLangProgramNode(listOf())
        }
    }
}

