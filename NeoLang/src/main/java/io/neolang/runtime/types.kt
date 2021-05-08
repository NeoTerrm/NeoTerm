package io.neolang.runtime

/**
 * @author kiva
 */
class NeoLangArray private constructor(
  val elements: List<NeoLangArrayElement>,
  override val size: Int = elements.size
) : Collection<NeoLangArrayElement> {
  companion object {
    internal class PrimaryElement(val primaryValue: NeoLangValue) : NeoLangArrayElement() {
      override fun eval(): NeoLangValue {
        return primaryValue
      }
    }

    internal class BlockElement(val blockContext: NeoLangContext) : NeoLangArrayElement() {
      override fun eval(key: String): NeoLangValue {
        return blockContext.getAttribute(key)
      }

      override fun isBlock(): Boolean {
        return true
      }
    }

    fun createFromContext(context: NeoLangContext): NeoLangArray {
      val elements = mutableListOf<NeoLangArrayElement>()
      context.getAttributes().entries.forEach {
        val index = it.key.toInt()
        elements.add(index, PrimaryElement(it.value))
      }
      context.children.forEach {
        val index = it.contextName.toInt()
        elements.add(index, BlockElement(it))
      }
      return NeoLangArray(elements)
    }
  }

  operator fun get(index: Int): NeoLangArrayElement {
    return elements[index]
  }

  override fun contains(element: NeoLangArrayElement): Boolean {
    return elements.contains(element)
  }

  override fun containsAll(elements: Collection<NeoLangArrayElement>): Boolean {
    return this.elements.containsAll(elements)
  }

  override fun isEmpty(): Boolean {
    return size == 0
  }

  override fun iterator(): Iterator<NeoLangArrayElement> {
    return elements.iterator()
  }
}

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
