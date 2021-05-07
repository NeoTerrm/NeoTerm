package io.neolang.runtime.type

/**
 * @author kiva
 */
open class NeoLangArrayElement {
  open fun eval(): NeoLangValue {
    return NeoLangValue.UNDEFINED
  }

  open fun eval(key: String): NeoLangValue {
    return NeoLangValue.UNDEFINED
  }

  open fun isBlock(): Boolean {
    return false
  }
}