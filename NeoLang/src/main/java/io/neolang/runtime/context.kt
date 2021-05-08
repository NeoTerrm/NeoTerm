package io.neolang.runtime

/**
 * @author kiva
 */
class NeoLangContext(val contextName: String) {
  companion object {
    private val emptyContext = NeoLangContext("<Context-Empty>")
  }

  private val attributes = mutableMapOf<String, NeoLangValue>()
  val children = mutableListOf<NeoLangContext>()
  var parent: NeoLangContext? = null

  fun defineAttribute(attributeName: String, attributeValue: NeoLangValue): NeoLangContext {
    attributes[attributeName] = attributeValue
    return this
  }

  fun getAttribute(attributeName: String): NeoLangValue {
    return attributes[attributeName] ?: parent?.getAttribute(attributeName) ?: NeoLangValue.UNDEFINED
  }

  fun getChild(contextName: String): NeoLangContext {
    var found: NeoLangContext? = null
    children.forEach {
      if (it.contextName == contextName) {
        found = it
      }
    }
    return found ?: emptyContext
  }

  fun getAttributes(): Map<String, NeoLangValue> {
    return attributes
  }
}
