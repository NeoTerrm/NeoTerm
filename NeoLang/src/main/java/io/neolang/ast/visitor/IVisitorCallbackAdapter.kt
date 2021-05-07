package io.neolang.ast.visitor

import io.neolang.runtime.context.NeoLangContext

/**
 * @author kiva
 */
open class IVisitorCallbackAdapter : IVisitorCallback {
  override fun onStart() {
  }

  override fun onFinish() {
  }

  override fun onEnterContext(contextName: String) {
  }

  override fun onExitContext() {
  }

  override fun getCurrentContext(): NeoLangContext {
    throw RuntimeException("getCurrentContext() not supported in this IVisitorCallback!")
  }
}