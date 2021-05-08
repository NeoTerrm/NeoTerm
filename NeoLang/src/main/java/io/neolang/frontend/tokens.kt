package io.neolang.frontend

import io.neolang.runtime.NeoLangValue

/**
 * @author kiva
 */
class NeoLangEOFToken : NeoLangToken(NeoLangTokenType.EOF, NeoLangTokenValue.EOF)

/**
 * @author kiva
 */

open class NeoLangToken(val tokenType: NeoLangTokenType, val tokenValue: NeoLangTokenValue) {
  var lineNumber = 0

  override fun toString(): String {
    return "Token { tokenType: $tokenType, tokenValue: $tokenValue };"
  }
}

/**
 * @author kiva
 */

enum class NeoLangTokenType {
  NUMBER,
  ID,
  STRING,
  BRACKET_START,
  BRACKET_END,
  ARRAY_START,
  ARRAY_END,
  COLON,
  COMMA,
  EOL,
  EOF,
}

/**
 * @author kiva
 */
class NeoLangTokenValue(val value: NeoLangValue) {

  override fun toString(): String {
    return value.asString()
  }

  companion object {
    val COLON = NeoLangTokenValue(NeoLangValue(":"))
    val COMMA = NeoLangTokenValue(NeoLangValue(","))
    val QUOTE = NeoLangTokenValue(NeoLangValue("\""))
    val EOF = NeoLangTokenValue(NeoLangValue("<EOF>"))

    val BRACKET_START = NeoLangTokenValue(NeoLangValue("{"))
    val BRACKET_END = NeoLangTokenValue(NeoLangValue("}"))
    val ARRAY_START = NeoLangTokenValue(NeoLangValue("["))
    val ARRAY_END = NeoLangTokenValue(NeoLangValue("]"))

    fun wrap(tokenText: String): NeoLangTokenValue {
      return when (tokenText) {
        COLON.value.asString() -> COLON
        COMMA.value.asString() -> COMMA
        QUOTE.value.asString() -> QUOTE
        BRACKET_START.value.asString() -> BRACKET_START
        BRACKET_END.value.asString() -> BRACKET_END
        ARRAY_START.value.asString() -> ARRAY_START
        ARRAY_END.value.asString() -> ARRAY_END
        else -> NeoLangTokenValue(NeoLangValue(tokenText))
      }
    }
  }
}
