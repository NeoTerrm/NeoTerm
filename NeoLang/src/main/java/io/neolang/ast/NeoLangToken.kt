package io.neolang.ast

/**
 * @author kiva
 */

open class NeoLangToken(val tokenType: NeoLangTokenType, val tokenValue: NeoLangTokenValue) {
  var lineNumber = 0

  override fun toString(): String {
    return "Token { tokenType: $tokenType, tokenValue: $tokenValue };"
  }
}
