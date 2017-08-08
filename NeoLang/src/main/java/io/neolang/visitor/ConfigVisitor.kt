package io.neolang.visitor

import io.neolang.ast.visitor.IVisitorCallback
import io.neolang.runtime.context.NeoLangContext
import io.neolang.runtime.type.NeoLangArray
import io.neolang.runtime.type.NeoLangValue

class ConfigVisitor : IVisitorCallback {
    private var currentContext: NeoLangContext? = null

    fun getContext(contextPath: Array<String>) : NeoLangContext {
        var context = getCurrentContext()
        contextPath.forEach {
            context = context.getChild(it)
        }
        return context
    }

    fun getAttribute(contextPath: Array<String>, attrName: String) : NeoLangValue {
        return getContext(contextPath).getAttribute(attrName)
    }

    fun getArray(contextPath: Array<String>, arrayName: String) : NeoLangArray {
        // We use NeoLangContext as arrays and array elements now
        return NeoLangArray.createFromContext(getContext(contextPath).getChild(arrayName))
    }

    override fun onStart() {
        currentContext = NeoLangContext("global")
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
        if (context != null && context.parent != null) {
            this.currentContext = context.parent
        }
    }

    override fun getCurrentContext(): NeoLangContext {
        return currentContext!!
    }
}