package io.neolang.ast

import io.neolang.runtime.type.NeoLangValue

/**
 * @author kiva
 */
class NeoLangTokenValue(val value: NeoLangValue) {

    override fun toString(): String {
        return value.asString()
    }

    companion object {
        val COLON = NeoLangTokenValue(NeoLangValue(":"))
        val BRACKET_START = NeoLangTokenValue(NeoLangValue("{"))
        val BRACKET_END = NeoLangTokenValue(NeoLangValue("}"))
        val QUOTE = NeoLangTokenValue(NeoLangValue("\""))
        val EOF = NeoLangTokenValue(NeoLangValue("<EOF>"))

        fun wrap(tokenText: String): NeoLangTokenValue {
            return when (tokenText) {
                COLON.value.asString() -> COLON
                BRACKET_START.value.asString() -> BRACKET_START
                BRACKET_END.value.asString() -> BRACKET_END
                QUOTE.value.asString() -> QUOTE
                else -> NeoLangTokenValue(NeoLangValue(tokenText))
            }
        }
    }
}
