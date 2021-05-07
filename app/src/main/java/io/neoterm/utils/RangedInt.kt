package io.neoterm.utils

/**
 * @author kiva
 */
class RangedInt(val number: Int, val range: IntRange) {
  fun increaseOne(): Int {
    var result = number + 1
    if (result > range.last) {
      result = 0
    }
    return result
  }

  fun decreaseOne(): Int {
    var result = number - 1
    if (result < 0) {
      result = range.last
    }
    return result
  }
}