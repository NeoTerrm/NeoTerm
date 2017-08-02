package io.neolang.ast

/**
 * @author kiva
 */
class NeoLangTokenValue(val value: String) {

    override fun toString(): String {
        return value
    }

    companion object {
        val COLON = NeoLangTokenValue(":")
        val BRACKET_START = NeoLangTokenValue("{")
        val BRACKET_END = NeoLangTokenValue("}")
        val QUOTE = NeoLangTokenValue("\"")
        val EOF = NeoLangTokenValue("<EOF>")

        fun wrap(tokenText: String): NeoLangTokenValue {
            return when (tokenText) {
                COLON.value -> COLON
                BRACKET_START.value -> BRACKET_START
                BRACKET_END.value -> BRACKET_END
                QUOTE.value -> QUOTE
                else -> NeoLangTokenValue(tokenText)
            }
        }
    }
}
