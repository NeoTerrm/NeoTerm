package io.neolang.ast.visitor

import io.neolang.ast.base.NeoLangAst
import io.neolang.ast.node.*


/**
 * grammar: [
 * program: group (group)*
 * group: attribute (attribute)*
 * attribute: ID COLON block
 * block: STRING | NUMBER | (BRACKET_START [group|] BRACKET_END) | (ARRAY_START [block(<,block>)+|] ARRAY_END)
 * ]
 */

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
            AstVisitorImpl.visitBlock(it.block, it.index.toString(), visitorCallback)
        }
        visitorCallback.onExitContext()
    }

    fun visitBlock(ast: NeoLangBlockNode, blockName: String, visitorCallback: IVisitorCallback) {
        val visitingNode = ast.ast
        when (visitingNode) {
            is NeoLangGroupNode -> {
                // is a sub block, e.g.
                // block: { $blockName: {} }

                // FIXME: Block in Array
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
                visitorCallback.getCurrentContext().defineAttribute(blockName, visitingNode.eval())
            }
            is NeoLangNumberNode -> {
                // block: { $blockName: 123.456 }
                visitorCallback.getCurrentContext().defineAttribute(blockName, visitingNode.eval())
            }
        }
    }

    fun visitStartAst(ast: NeoLangAst, visitorCallback: IVisitorCallback) {
        when (ast) {
            is NeoLangProgramNode -> AstVisitorImpl.visitProgram(ast, visitorCallback)
            is NeoLangGroupNode -> AstVisitorImpl.visitGroup(ast, visitorCallback)
            is NeoLangArrayNode -> AstVisitorImpl.visitArray(ast, visitorCallback)
        }
    }
}
