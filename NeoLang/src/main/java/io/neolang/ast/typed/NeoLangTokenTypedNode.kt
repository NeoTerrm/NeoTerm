package io.neolang.ast.typed

import io.neolang.ast.base.NeoLangAstBaseNode
import io.neolang.ast.NeoLangToken

/**
 * @author kiva
 */
open class NeoLangTokenTypedNode(val token: NeoLangToken) : NeoLangAstBaseNode() {
    override fun toString(): String {
        return "${javaClass.simpleName} { token: $token }"
    }
}