package io.neoterm.frontend.config

import io.neolang.ast.visitor.IVisitorCallback
import io.neolang.runtime.context.NeoLangContext
import io.neolang.runtime.type.NeoLangValue
import java.util.*

class ConfigVisitor : IVisitorCallback {
    private val emptyContext = NeoLangContext("<NeoTerm-Empty-Safety>")
    private val contextStack = Stack<NeoLangContext>()
    private val definedContext = mutableListOf<NeoLangContext>()

    fun getContext(contextName: String): NeoLangContext {
        definedContext.forEach {
            if (it.contextName == contextName) {
                return it
            }
        }
        return emptyContext
    }

    fun getAttribute(contextName: String, attrName: String): NeoLangValue {
        return getContext(contextName).getAttribute(attrName)
    }

    override fun onStart() {
        onEnterContext("global")
    }

    override fun onFinish() {
        while (contextStack.isNotEmpty()) {
            onExitContext()
        }
    }

    override fun onEnterContext(contextName: String) {
        val context = NeoLangContext(contextName)
        contextStack.push(context)
    }

    override fun onExitContext() {
        val context = contextStack.pop()
        definedContext.add(context)
    }

    override fun getCurrentContext(): NeoLangContext {
        return contextStack.peek()
    }
}