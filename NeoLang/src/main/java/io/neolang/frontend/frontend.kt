package io.neolang.frontend

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
    val tokens = ArrayList<NeoLangToken>()
    currentPosition = 0
    lineNumber = 1

    if (programCode.isNotEmpty()) {
      currentChar = programCode[currentPosition]

      while (currentPosition < programCode.length) {
        val token = nextToken
        if (token is NeoLangEOFToken) {
          break
        }
        tokens.add(token)
      }
    }
    return tokens
  }

  private fun moveToNextChar(eofThrow: Boolean = false): Boolean {
    val programCode = this.programCode ?: return false
    currentPosition++
    if (currentPosition >= programCode.length) {
      if (eofThrow) {
        throw InvalidTokenException("Unexpected EOF near `$currentChar' in line $lineNumber")
      }
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
        || currentChar == '\r'
      ) {
        if (currentChar == '\n') {
          ++lineNumber
        }
        // Skip white chars
        if (!moveToNextChar()) {
          return NeoLangEOFToken()
        }
      }

      if (currentPosition >= programCode.length) {
        return NeoLangEOFToken()
      }

      val currentToken = NeoLangTokenValue.wrap(currentChar.toString())
      val token: NeoLangToken = when (currentToken) {
        NeoLangTokenValue.COLON -> {
          moveToNextChar(eofThrow = true)
          NeoLangToken(NeoLangTokenType.COLON, currentToken)
        }
        NeoLangTokenValue.BRACKET_START -> {
          moveToNextChar(eofThrow = true)
          NeoLangToken(NeoLangTokenType.BRACKET_START, currentToken)
        }
        NeoLangTokenValue.BRACKET_END -> {
          moveToNextChar()
          NeoLangToken(NeoLangTokenType.BRACKET_END, currentToken)
        }
        NeoLangTokenValue.ARRAY_START -> {
          moveToNextChar()
          NeoLangToken(NeoLangTokenType.ARRAY_START, currentToken)
        }
        NeoLangTokenValue.ARRAY_END -> {
          moveToNextChar()
          NeoLangToken(NeoLangTokenType.ARRAY_END, currentToken)
        }
        NeoLangTokenValue.COMMA -> {
          moveToNextChar(eofThrow = true)
          NeoLangToken(NeoLangTokenType.COMMA, currentToken)
        }
        NeoLangTokenValue.QUOTE -> {
          NeoLangToken(NeoLangTokenType.STRING, NeoLangTokenValue.wrap(getNextTokenAsString()))
        }
        else -> {
          if (currentChar.isNumber()) {
            NeoLangToken(NeoLangTokenType.NUMBER, NeoLangTokenValue.wrap(getNextTokenAsNumber()))
          } else if (isIdentifier(currentChar, true)) {
            NeoLangToken(NeoLangTokenType.ID, NeoLangTokenValue.wrap(getNextTokenAsId()))
          } else {
            throw InvalidTokenException("Unexpected character near line $lineNumber: $currentChar")
          }
        }
      }

      token.lineNumber = lineNumber
      return token
    }

  private fun getNextTokenAsString(): String {
    // Skip start quote
    // and a single quote is now allowed
    moveToNextChar(eofThrow = true)
    val builder = StringBuilder()

    var loop = true
    while (loop && currentChar != NeoLangTokenValue.QUOTE.value.asString()[0]) {
      // NeoLang does not support escaped char
//            if (currentChar == '\\') {
//                builder.append('\\')
//                moveToNextChar(eofThrow = true)
//            }
      builder.append(currentChar)
      loop = moveToNextChar()
    }

    // Skip end quote
    moveToNextChar()
    return builder.toString()
  }

  private fun getNextTokenAsNumber(): String {
    var numberValue: Double = (currentChar.toInt() - '0'.toInt()).toDouble()

    // Four types of numbers are supported:
    // Dec(123) Hex(0x123) Oct(017) Bin(0b11)

    // Dec
    if (numberValue > 0) {
      numberValue = getNextDecimalNumber(numberValue)
    } else {
      // is 0
      if (!moveToNextChar()) {
        return numberValue.toString()
      }

      // Hex
      if (currentChar == 'x' || currentChar == 'X') {
        numberValue = getNextHexNumber(numberValue)
      } else if (currentChar == 'b' || currentChar == 'B') {
        numberValue = getNextBinaryNumber(numberValue)
      } else {
        numberValue = getNextOctalNumber(numberValue)
      }
    }

    return numberValue.toString()
  }

  private fun getNextOctalNumber(numberValue: Double): Double {
    var value: Double = numberValue
    var loop = true
    while (loop && currentChar in ('0'..'7')) {
      value = value * 8 + currentChar.toNumber()
      loop = moveToNextChar()
    }
    return value
  }

  private fun getNextBinaryNumber(numberValue: Double): Double {
    var value = numberValue
    var loop = moveToNextChar() // skip 'b' or 'B'
    while (loop && currentChar in ('0'..'1')) {
      value += value * 2 + currentChar.toNumber()
      loop = moveToNextChar()
    }
    return value
  }

  private fun getNextHexNumber(numberValue: Double): Double {
    var value = numberValue
    var loop = moveToNextChar() // skip 'x' or 'X'
    while (loop && (currentChar.isHexNumber())) {
      value *= 16 + (currentChar.toInt().and(15)) + if (currentChar >= 'A') 9 else 0
      loop = moveToNextChar()
    }
    return value
  }

  private fun getNextDecimalNumber(numberValue: Double): Double {
    var floatPointMeet = false
    var floatPart: Double = 0.0
    var floatNumberCounter = 1
    var value = numberValue

    var loop = moveToNextChar()
    while (loop) {
      if (currentChar.isNumber()) {
        if (floatPointMeet) {
          floatPart = floatPart * 10 + currentChar.toNumber()
          floatNumberCounter *= 10
        } else {
          value = value * 10 + currentChar.toNumber()
        }
        loop = moveToNextChar()

      } else if (currentChar == '.') {
        floatPointMeet = true
        loop = moveToNextChar()
      } else {
        break
      }
    }
    return value + floatPart / floatNumberCounter
  }

  private fun getNextTokenAsId(): String {
    return buildString {
      while (isIdentifier(currentChar, false)) {
        append(currentChar)
        if (!moveToNextChar()) {
          break
        }
      }
    }
  }

  private fun isIdentifier(tokenChar: Char, isFirstChar: Boolean): Boolean {
    val isId = (tokenChar in 'a'..'z')
      || (tokenChar in 'A'..'Z')
      || ("_-#$".contains(tokenChar))
    return if (isFirstChar) isId else (isId || (tokenChar in '0'..'9'))
  }

  private fun Char.toNumber(): Int {
    return if (isNumber()) {
      this.toInt() - '0'.toInt()
    } else 0
  }

  private fun Char.isNumber(): Boolean {
    return this in ('0'..'9')
  }

  private fun Char.isHexNumber(): Boolean {
    return this.isNumber()
      || this in ('a'..'f')
      || this in ('A'..'F')
  }
}

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
      throw InvalidTokenException(
        "Unexpected token `${currentToken.tokenValue}' typed " +
          "`${currentToken.tokenType}' near line ${currentToken.lineNumber}, " +
          "expected $tokenType",
      )
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

  /**
   * @param attrName Only available when group is a attribute value
   */
  private fun group(): NeoLangGroupNode? {
    val token = currentToken ?: throw InvalidTokenException("Unexpected token: null")

    var attr = attribute()
    if (attr != null) {
      val attributes = mutableListOf(attr)

      while (token.tokenType !== NeoLangTokenType.EOF
        && token.tokenType !== NeoLangTokenType.BRACKET_END
        && token.tokenType !== NeoLangTokenType.ARRAY_END
      ) {
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
      val elements = mutableListOf(NeoLangArrayNode.Companion.ArrayElement(index++, block))

      if (match(NeoLangTokenType.COMMA)) {
        // More than one elements
        while (token.tokenType !== NeoLangTokenType.EOF
          && token.tokenType !== NeoLangTokenType.ARRAY_END
        ) {
          block = blockNonArrayElement(arrayName)
          if (block == null) {
            break
          }
          elements.add(NeoLangArrayNode.Companion.ArrayElement(index++, block))

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
   * @param attrName The block holder's name
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

  /**
   * @param attrName Only available when group is a attribute value
   */
  private fun blockNonArrayElement(attrName: NeoLangStringNode?): NeoLangBlockNode? {
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

open class InvalidTokenException(message: String) : ParseException(message)
open class ParseException(message: String) : RuntimeException(message)
