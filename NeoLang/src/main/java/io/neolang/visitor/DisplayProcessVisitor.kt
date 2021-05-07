package io.neolang.visitor

import io.neolang.ast.visitor.IVisitorCallbackAdapter
import io.neolang.runtime.context.NeoLangContext
import java.util.*

/**
 * @author kiva
 */
class DisplayProcessVisitor : IVisitorCallbackAdapter() {
  private val contextStack = Stack<NeoLangContext>()

  override fun onStart() {
    println(">>> Start")
    onEnterContext("global")
  }

  override fun onFinish() {
    while (contextStack.isNotEmpty()) {
      onExitContext()
    }
    println(">>> Finish")
  }

  override fun onEnterContext(contextName: String) {
    val context = NeoLangContext(contextName)
    contextStack.push(context)
    println(">>> Entering Context `$contextName'")
  }

  override fun onExitContext() {
    val context = contextStack.pop()
    println(">>> Exiting & Dumping Context ${context.contextName}")
    context.getAttributes().entries.forEach {
      println("     > [${it.key}]: ${it.value.asString()}")
    }
  }

  override fun getCurrentContext(): NeoLangContext {
    return contextStack.peek()
  }
}
