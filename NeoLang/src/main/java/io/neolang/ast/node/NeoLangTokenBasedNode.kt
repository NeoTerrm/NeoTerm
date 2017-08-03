package io.neolang.ast.node

import io.neolang.ast.base.NeoLangAstBaseNode
import io.neolang.ast.NeoLangToken
import io.neolang.runtime.type.NeoLangValue

/**
 * @author kiva
 */
open class NeoLangTokenBasedNode(val token: NeoLangToken) : NeoLangAstBaseNode() {
    override fun toString(): String {
        return "${javaClass.simpleName} { token: $token }"
    }

    fun eval() :NeoLangValue {
        return token.tokenValue.value
    }
}