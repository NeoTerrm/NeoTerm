package io.neolang.ast.visitor

import io.neolang.runtime.context.NeoLangContext

/**
 * @author kiva
 */
interface IVisitorCallback {
  fun onStart()

  fun onFinish()

  fun onEnterContext(contextName: String)

  fun onExitContext()

  fun getCurrentContext(): NeoLangContext
}
