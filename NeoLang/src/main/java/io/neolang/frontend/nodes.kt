package io.neolang.frontend

import io.neolang.runtime.NeoLangValue

/**
 * @author kiva
 */
open class NeoLangAst {
  fun visit(): VisitorFactory {
    return VisitorFactory(this)
  }
}

open class NeoLangBaseNode : NeoLangAst()

/**
 * @author kiva
 */
class NeoLangArrayNode(val arrayNameNode: NeoLangStringNode, val elements: Array<ArrayElement>) : NeoLangBaseNode() {
  companion object {
    class ArrayElement(val index: Int, val block: NeoLangBlockNode)
  }
}

/**
 * @author kiva
 */
open class NeoLangAstBasedNode(val ast: NeoLangBaseNode) : NeoLangBaseNode() {
  override fun toString(): String {
    return "${javaClass.simpleName} { ast: $ast }"
  }
}

/**
 * @author kiva
 */
class NeoLangAttributeNode(val stringNode: NeoLangStringNode, val blockNode: NeoLangBlockNode) : NeoLangBaseNode() {

  override fun toString(): String {
    return "NeoLangAttributeNode { stringNode: $stringNode, block: $blockNode }"
  }
}

/**
 * @author kiva
 */
class NeoLangBlockNode(blockElement: NeoLangBaseNode) : NeoLangAstBasedNode(blockElement) {
  companion object {
    fun emptyNode(): NeoLangBlockNode {
      return NeoLangBlockNode(NeoLangDummyNode())
    }
  }
}

/**
 * @author kiva
 */
class NeoLangDummyNode : NeoLangBaseNode()

/**
 * @author kiva
 */
class NeoLangGroupNode(val attributes: Array<NeoLangAttributeNode>) : NeoLangBaseNode() {

  override fun toString(): String {
    return "NeoLangGroupNode { attrs: $attributes }"
  }

  companion object {
    fun emptyNode(): NeoLangGroupNode {
      return NeoLangGroupNode(arrayOf())
    }
  }
}

/**
 * @author kiva
 */
class NeoLangNumberNode(token: NeoLangToken) : NeoLangTokenBasedNode(token)

/**
 * @author kiva
 */

class NeoLangProgramNode(val groups: List<NeoLangGroupNode>) : NeoLangBaseNode() {

  override fun toString(): String {
    return "NeoLangProgramNode { groups: $groups }"
  }

  companion object {
    fun emptyNode(): NeoLangProgramNode {
      return NeoLangProgramNode(listOf())
    }
  }
}

/**
 * @author kiva
 */
class NeoLangStringNode(token: NeoLangToken) : NeoLangTokenBasedNode(token)

/**
 * @author kiva
 */
open class NeoLangTokenBasedNode(val token: NeoLangToken) : NeoLangBaseNode() {
  override fun toString(): String {
    return "${javaClass.simpleName} { token: $token }"
  }

  fun eval(): NeoLangValue {
    return token.tokenValue.value
  }
}
