package io.neolang.ast.visitor

import io.neolang.ast.base.NeoLangAst

/**
 * @author kiva
 */

class VisitorFactory(private val ast: NeoLangAst) {

  fun getVisitor(callbackInterface: Class<out IVisitorCallback>): AstVisitor? {
    try {
      return AstVisitor(ast, callbackInterface.newInstance())
    } catch (e: Exception) {
      return null
    }
  }
}
