package io.neolang.runtime.type

/**
 * @author kiva
 */
class NeoLangValue(private val rawValue: Any) {
  fun asString(): String {
    return rawValue.toString()
  }

  fun asNumber(): Double {
    if (rawValue is Array<*>) {
      return 0.0
    }

    try {
      return rawValue.toString().toDouble()
    } catch (e: Throwable) {
      return 0.0
    }
  }

  fun isValid(): Boolean {
    return this != UNDEFINED
  }

  companion object {
    val UNDEFINED = NeoLangValue("<undefined>")
  }
}