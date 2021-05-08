package io.neolang.frontend

import io.neolang.runtime.NeoLangArray
import io.neolang.runtime.NeoLangContext
import io.neolang.runtime.NeoLangValue
import java.util.*

class ConfigVisitor : IVisitorCallback {
  private var rootContext: NeoLangContext? = null
  private var currentContext: NeoLangContext? = null

  fun getRootContext(): NeoLangContext {
    return rootContext!!
  }

  fun getContext(contextPath: Array<String>): NeoLangContext {
    var context = getCurrentContext()
    contextPath.forEach {
      context = context.getChild(it)
    }
    return context
  }

  fun getAttribute(contextPath: Array<String>, attrName: String): NeoLangValue {
    return getContext(contextPath).getAttribute(attrName)
  }

  fun getArray(contextPath: Array<String>, arrayName: String): NeoLangArray {
    // We use NeoLangContext as arrays and array elements now
    return NeoLangArray.createFromContext(getContext(contextPath).getChild(arrayName))
  }

  fun getStringValue(path: Array<String>, name: String): String? {
    val value = this.getAttribute(path, name)
    return if (value.isValid()) value.asString() else null
  }

  fun getBooleanValue(path: Array<String>, name: String): Boolean? {
    val value = this.getAttribute(path, name)
    return if (value.isValid()) value.asString() == "true" else null
  }

  override fun onStart() {
    currentContext = NeoLangContext("global")
    rootContext = currentContext
  }

  override fun onFinish() {
    var context = currentContext
    while (context != null && context.parent != null) {
      context = context.parent
    }
    this.currentContext = context
  }

  override fun onEnterContext(contextName: String) {
    val newContext = NeoLangContext(contextName)
    newContext.parent = currentContext
    currentContext!!.children.add(newContext)
    currentContext = newContext
  }

  override fun onExitContext() {
    val context = currentContext
    if (context?.parent != null) {
      this.currentContext = context.parent
    }
  }

  override fun getCurrentContext(): NeoLangContext {
    return currentContext!!
  }
}

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
