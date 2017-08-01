package io.neolang.ast.base

import io.neolang.ast.visitor.NeoLangAstVisitor

/**
 * @author kiva
 */
open class NeoLangAst {
    fun visit(): NeoLangAstVisitor {
        return NeoLangAstVisitor(this)
    }
}