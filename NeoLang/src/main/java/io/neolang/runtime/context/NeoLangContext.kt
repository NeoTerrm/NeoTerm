package io.neolang.runtime.context

import io.neolang.runtime.type.NeoLangValue

/**
 * @author kiva
 */
class NeoLangContext(val contextName: String) {
    private val attributes = mutableMapOf<String, NeoLangValue>()

    fun defineAttribute(attributeName: String, attributeValue: NeoLangValue): NeoLangContext {
        attributes[attributeName] = attributeValue
        return this
    }

    fun getAttribute(attributeName: String): NeoLangValue {
        return attributes[attributeName] ?: NeoLangValue.UNDEFINED
    }

    fun getAttributes(): Map<String, NeoLangValue> {
        return attributes
    }
}
