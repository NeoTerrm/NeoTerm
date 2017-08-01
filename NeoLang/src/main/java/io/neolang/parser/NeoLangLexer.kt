package io.neolang.parser

import io.neolang.ast.NeoLangEOFToken
import io.neolang.ast.NeoLangToken
import io.neolang.ast.NeoLangTokenType
import io.neolang.ast.NeoLangTokenValue
import java.util.*

/**
 * grammar: [
 * prog: group (group)*
 * group: attribute (attribute)*
 * attribute: TEXT COLON block
 * block: NUMBER | TEXT | (BRACKET_START group BRACKET_STOP)
 * ]
 */

/**
 * @author kiva
 */
class NeoLangLexer {
    private var programCode: String? = null
    private var currentPosition: Int = 0
    private var currentChar: Char = ' '
    private var lineNumber = 0

    internal fun setInputSource(programCode: String?) {
        this.programCode = programCode
    }

    internal fun lex(): List<NeoLangToken> {
        val programCode = this.programCode ?: return listOf()
        currentPosition = 0
        lineNumber = 1
        currentChar = programCode[currentPosition]

        val tokens = ArrayList<NeoLangToken>()
        while (currentPosition < programCode.length) {
            val token = nextToken
            if (token is NeoLangEOFToken) {
                break
            }
            tokens.add(token)
        }
        return tokens
    }

    private fun moveToNextChar(): Boolean {
        val programCode = this.programCode ?: return false
        currentPosition++
        if (currentPosition >= programCode.length) {
            return false
        } else {
            currentChar = programCode[currentPosition]
            return true
        }
    }

    private val nextToken: NeoLangToken
        get() {
            val programCode = this.programCode ?: return NeoLangEOFToken()

            while (currentChar == ' '
                    || currentChar == '\t'
                    || currentChar == '\n'
                    || currentChar == '\r') {
                if (currentChar == '\n') {
                    ++lineNumber
                }
                // Skip white chars
                moveToNextChar()
            }

            if (currentPosition >= programCode.length) {
                return NeoLangEOFToken()
            }

            val currentToken = NeoLangTokenValue.wrap(currentChar.toString())
            val token: NeoLangToken = when (currentToken) {
                NeoLangTokenValue.COLON -> {
                    moveToNextChar()
                    NeoLangToken(NeoLangTokenType.COLON, currentToken)
                }
                NeoLangTokenValue.BRACKET_START -> {
                    moveToNextChar()
                    NeoLangToken(NeoLangTokenType.BRACKET_START, currentToken)
                }
                NeoLangTokenValue.BRACKET_END -> {
                    moveToNextChar()
                    NeoLangToken(NeoLangTokenType.BRACKET_END, currentToken)
                }
                NeoLangTokenValue.QUOTE -> {
                    moveToNextChar()
                    NeoLangToken(NeoLangTokenType.QUOTE, currentToken)
                }
                else -> {
                    if (Character.isDigit(currentChar)) {
                        NeoLangToken(NeoLangTokenType.NUMBER, NeoLangTokenValue.wrap(getNextTokenAsNumber()))
                    } else if (Character.isLetterOrDigit(currentChar)) {
                        NeoLangToken(NeoLangTokenType.STRING, NeoLangTokenValue.wrap(getNextTokenAsString()))
                    } else {
                        throw InvalidTokenException("Unexpected character: " + currentChar)
                    }
                }
            }

            token.lineNumber = lineNumber
            return token
        }

    private fun getNextTokenAsNumber(): String {
        return buildString {
            while (Character.isDigit(currentChar)) {
                append(currentChar)
                if (!moveToNextChar()) {
                    break
                }
            }
        }
    }

    private fun getNextTokenAsString(): String {
        return buildString {
            while (Character.isLetterOrDigit(currentChar)) {
                append(currentChar)
                if (!moveToNextChar()) {
                    break
                }
            }
        }
    }

}
