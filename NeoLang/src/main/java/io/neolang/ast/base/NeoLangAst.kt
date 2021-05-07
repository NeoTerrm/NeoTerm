package io.neolang.ast.base

import io.neolang.ast.visitor.VisitorFactory

/**
 * @author kiva
 */
open class NeoLangAst {
  fun visit(): VisitorFactory {
    return VisitorFactory(this)
  }
}