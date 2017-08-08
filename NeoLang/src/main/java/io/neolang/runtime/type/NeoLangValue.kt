package io.neolang.runtime.type

import io.neolang.ast.node.NeoLangBlockNode

/**
 * @author kiva
 */
class NeoLangValue(private val rawValue: Any) {
    fun asString(): String {
        if (rawValue is Array<*>) {
            val array = asArray()

            return buildString {
                append("Array [ ")
                array.forEachIndexed { index, value ->
                    append(value.asString())
                    if (index != array.size - 1) {
                        append(", ")
                    }
                }
                append(" ]")
            }
        }

        return rawValue.toString()
    }

    fun asNumber(): Double {
        if (rawValue is Array<*>) {
            return 0.0
        }

        try {
            return rawValue.toString().toDouble()
        } catch (e: NumberFormatException) {
            return 0.0
        }
    }

    fun asArray(): Array<NeoLangValue> {
        return castArrayOrNull(rawValue) ?: arrayOf()
    }

    @Suppress("UNCHECKED_CAST")
    private fun castArrayOrNull(rawValue: Any): Array<NeoLangValue>? {
        return if (rawValue is Array<*> && rawValue.isNotEmpty() && rawValue[0] is NeoLangValue)
            rawValue as Array<NeoLangValue>
        else null
    }

    fun isValid(): Boolean {
        return this != UNDEFINED
    }

    companion object {
        val UNDEFINED = NeoLangValue("<undefined>")
    }
}