package io.neolang.runtime.type

/**
 * @author kiva
 */
class NeoLangValue(private val rawValue: Any) {
    fun asString() : String {
        return rawValue.toString()
    }

    fun asNumber() : Double {
        try {
            return asString().toDouble()
        } catch (e: NumberFormatException) {
            return 0.0
        }
    }

    companion object {
        val UNDEFINED = NeoLangValue("<undefined>")
    }
}