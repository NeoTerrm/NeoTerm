package io.neolang.frontend

import io.neolang.runtime.NeoLangContext
import io.neolang.runtime.NeoLangValue

/**
 * @author kiva
 */
class AstVisitor(private val ast: NeoLangAst, private val visitorCallback: IVisitorCallback) {
  fun start() {
    AstVisitorImpl.visitStartAst(ast, visitorCallback)
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : IVisitorCallback> getCallback(): T {
    return visitorCallback as T
  }
}

/**
 * @author kiva
 */
internal object AstVisitorImpl {
  fun visitProgram(ast: NeoLangProgramNode, visitorCallback: IVisitorCallback) {
    visitorCallback.onStart()
    ast.groups.forEach { visitGroup(it, visitorCallback) }
    visitorCallback.onFinish()
  }

  fun visitGroup(ast: NeoLangGroupNode, visitorCallback: IVisitorCallback) {
    ast.attributes.forEach {
      visitAttribute(it, visitorCallback)
    }
  }

  fun visitAttribute(ast: NeoLangAttributeNode, visitorCallback: IVisitorCallback) {
    visitBlock(ast.blockNode, ast.stringNode.eval().asString(), visitorCallback)
  }

  fun visitArray(ast: NeoLangArrayNode, visitorCallback: IVisitorCallback) {
    val arrayName = ast.arrayNameNode.eval().asString()

    visitorCallback.onEnterContext(arrayName)
    ast.elements.forEach {
      AstVisitorImpl.visitArrayElementBlock(it.block, it.index, visitorCallback)
//            AstVisitorImpl.visitBlock(it.block, it.index.toString(), visitorCallback)
    }
    visitorCallback.onExitContext()
  }

  fun visitArrayElementBlock(ast: NeoLangBlockNode, index: Int, visitorCallback: IVisitorCallback) {
    val visitingNode = ast.ast
    when (visitingNode) {
      is NeoLangGroupNode -> {
        // is a sub block, e.g.
        // block: { $blockName: {} }
        visitorCallback.onEnterContext(index.toString())
        AstVisitorImpl.visitGroup(visitingNode, visitorCallback)
        visitorCallback.onExitContext()
      }
      is NeoLangStringNode -> {
        definePrimaryData(index.toString(), visitingNode.eval(), visitorCallback)
      }
      is NeoLangNumberNode -> {
        definePrimaryData(index.toString(), visitingNode.eval(), visitorCallback)
      }
    }
  }

  fun visitBlock(ast: NeoLangBlockNode, blockName: String, visitorCallback: IVisitorCallback) {
    val visitingNode = ast.ast
    when (visitingNode) {
      is NeoLangGroupNode -> {
        // is a sub block, e.g.
        // block: { $blockName: {} }

        visitorCallback.onEnterContext(blockName)
        AstVisitorImpl.visitGroup(visitingNode, visitorCallback)
        visitorCallback.onExitContext()
      }
      is NeoLangArrayNode -> {
        // array: [ "a", "b", "c", 1, 2, 3 ]
        AstVisitorImpl.visitArray(visitingNode, visitorCallback)
      }
      is NeoLangStringNode -> {
        // block: { $blockName: "hello" }
        definePrimaryData(blockName, visitingNode.eval(), visitorCallback)
      }
      is NeoLangNumberNode -> {
        // block: { $blockName: 123.456 }
        definePrimaryData(blockName, visitingNode.eval(), visitorCallback)
      }
    }
  }

  private fun definePrimaryData(name: String, value: NeoLangValue, visitorCallback: IVisitorCallback) {
    visitorCallback.getCurrentContext().defineAttribute(name, value)
  }

  fun visitStartAst(ast: NeoLangAst, visitorCallback: IVisitorCallback) {
    when (ast) {
      is NeoLangProgramNode -> AstVisitorImpl.visitProgram(ast, visitorCallback)
      is NeoLangGroupNode -> AstVisitorImpl.visitGroup(ast, visitorCallback)
      is NeoLangArrayNode -> AstVisitorImpl.visitArray(ast, visitorCallback)
    }
  }
}

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
