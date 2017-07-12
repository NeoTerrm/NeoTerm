package io.neolang.token

/**
 * @author kiva
 */
class Token {
    lateinit var type: TokenType
    lateinit var value: Any
    lateinit var nextToken: Token
}