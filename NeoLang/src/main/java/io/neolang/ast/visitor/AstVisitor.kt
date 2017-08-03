package io.neolang.ast.visitor

import io.neolang.ast.base.NeoLangAst

/**
 * @author kiva
 */
class AstVisitor(private val ast: NeoLangAst, private val visitorCallback: IVisitorCallback) {
    fun start() {
        AstVisitorImpl.visitStartAst(ast, visitorCallback)
    }
}
