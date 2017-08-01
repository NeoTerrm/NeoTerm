package io.neolang.ast

/**
 * @author kiva
 */
enum class NeoLangTokenValue(val value: String) {
    COLON(":"),
    BRACKET_START("{"),
    BRACKET_END("}"),
    QUOTE("\""),
    EOF("");

    companion object {
        fun wrap(tokenText: String): NeoLangTokenValue {
            return when (tokenText) {
                COLON.value -> COLON
                BRACKET_START.value -> BRACKET_START
                BRACKET_END.value -> BRACKET_END
                QUOTE.value -> QUOTE
                else -> EOF
            }
        }
    }
}
