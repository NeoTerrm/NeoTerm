package io.neolang.ast

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
