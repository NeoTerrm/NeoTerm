package io.neolang.parser

import io.neolang.ast.NeoLangToken
import io.neolang.ast.NeoLangTokenType
import io.neolang.ast.NeoLangTokenValue
import io.neolang.ast.base.NeoLangAst
import io.neolang.ast.node.*
import io.neolang.runtime.type.NeoLangArrayElement

/**
 * @author kiva
 */
class NeoLangParser {
    private val lexer = NeoLangLexer()
    private var tokens = mutableListOf<NeoLangToken>()
    private var currentPosition: Int = 0
    private var currentToken: NeoLangToken? = null

    fun setInputSource(programCode: String?) {
        lexer.setInputSource(programCode)
    }

    fun parse(): NeoLangAst {
        return updateParserStatus(lexer.lex()) ?: throw ParseException("AST is null")
    }

    private fun updateParserStatus(tokens: List<NeoLangToken>): NeoLangAst? {
        if (tokens.isEmpty()) {
            // Allow empty program
            return NeoLangProgramNode.emptyNode()
        }

        this.tokens.clear()
        this.tokens.addAll(tokens)
        currentPosition = 0
        currentToken = tokens[currentPosition]
        return program()
    }

    private fun match(tokenType: NeoLangTokenType, errorThrow: Boolean = false): Boolean {
        val currentToken = this.currentToken ?: throw InvalidTokenException("Unexpected token: null")

        if (currentToken.tokenType === tokenType) {
            currentPosition++
            if (currentPosition >= tokens.size) {
                this.currentToken = NeoLangToken(NeoLangTokenType.EOF, NeoLangTokenValue.EOF)
            } else {
                this.currentToken = tokens[currentPosition]
            }
            return true

        } else if (errorThrow) {
            throw InvalidTokenException("Unexpected token `${currentToken.tokenValue}' typed " +
                    "`${currentToken.tokenType}' near line ${currentToken.lineNumber}, " +
                    "expected $tokenType")
        }

        return false
    }

    private fun program(): NeoLangProgramNode {
        val token = currentToken

        var group = group()
        if (group != null) {
            val groups = mutableListOf(group)
            while (token?.tokenType !== NeoLangTokenType.EOF) {
                group = group()
                if (group == null) {
                    break
                }
                groups.add(group)
            }
            return NeoLangProgramNode(groups)
        }

        return NeoLangProgramNode.emptyNode()
    }

    private fun group(): NeoLangGroupNode? {
        val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")

        var attr = attribute()
        if (attr != null) {
            val attributes = mutableListOf(attr)

            while (token.tokenType !== NeoLangTokenType.EOF
                    && token.tokenType !== NeoLangTokenType.BRACKET_END
                    && token.tokenType !== NeoLangTokenType.ARRAY_END) {
                attr = attribute()
                if (attr == null) {
                    break
                }
                attributes.add(attr)
            }
            return NeoLangGroupNode(attributes.toTypedArray())
        }

        return null
    }

    private fun attribute(): NeoLangAttributeNode? {
        val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")
        if (match(NeoLangTokenType.ID)) {
            match(NeoLangTokenType.COLON, errorThrow = true)

            val attrName = NeoLangStringNode(token)

            val block = block(attrName) ?: NeoLangBlockNode.emptyNode()
            return NeoLangAttributeNode(attrName, block)
        }
        return null
    }

    private fun array(arrayName: NeoLangStringNode): NeoLangArrayNode? {
        val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")


        // TODO: Multiple Array
        var block = blockNonArrayElement(arrayName)
        var index = 0

        if (block != null) {

            val elements = mutableListOf(NeoLangArrayElement(index++, block))

            if (match(NeoLangTokenType.COMMA)) {
                // More than one elements
                while (token.tokenType !== NeoLangTokenType.EOF
                        && token.tokenType !== NeoLangTokenType.ARRAY_END) {
                    block = blockNonArrayElement(arrayName)
                    if (block == null) {
                        break
                    }
                    elements.add(NeoLangArrayElement(index++, block))

                    // Meet the last element
                    if (!match(NeoLangTokenType.COMMA)) {
                        break
                    }
                }
            }

            return NeoLangArrayNode(arrayName, elements.toTypedArray())
        }

        return null
    }


    /**
     * @attrName The block holder's name
     */
    private fun block(attrName: NeoLangStringNode): NeoLangBlockNode? {
        val block = blockNonArrayElement(attrName)
        if (block != null) {
            return block
        }

        val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")
        when (token.tokenType) {
            NeoLangTokenType.ARRAY_START -> {
                match(NeoLangTokenType.ARRAY_START, errorThrow = true)
                val array = array(attrName)
                match(NeoLangTokenType.ARRAY_END, errorThrow = true)

                // Allow empty arrays
                return if (array != null) NeoLangBlockNode(array) else NeoLangBlockNode.emptyNode()
            }

            else -> throw InvalidTokenException("Unexpected token `${token.tokenValue}' typed `${token.tokenType}' for block")
        }
    }

    private fun blockNonArrayElement(attrName: NeoLangStringNode): NeoLangBlockNode? {
        val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")

        return when (token.tokenType) {
            NeoLangTokenType.NUMBER -> {
                match(NeoLangTokenType.NUMBER, errorThrow = true)
                return NeoLangBlockNode(NeoLangNumberNode(token))
            }
            NeoLangTokenType.ID -> {
                match(NeoLangTokenType.ID, errorThrow = true)
                return NeoLangBlockNode(NeoLangStringNode(token))
            }
            NeoLangTokenType.STRING -> {
                match(NeoLangTokenType.STRING, errorThrow = true)
                return NeoLangBlockNode(NeoLangStringNode(token))
            }
            NeoLangTokenType.BRACKET_START -> {
                match(NeoLangTokenType.BRACKET_START, errorThrow = true)
                val group = group()
                match(NeoLangTokenType.BRACKET_END, errorThrow = true)

                // Allow empty blocks
                return if (group != null) NeoLangBlockNode(group) else NeoLangBlockNode.emptyNode()
            }
            else -> null
        }
    }
}
