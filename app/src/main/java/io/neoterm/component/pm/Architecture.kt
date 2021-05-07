package io.neoterm.component.pm

/**
 * @author kiva
 */

enum class Architecture {
  ALL, ARM, AARCH64, X86, X86_64;

  companion object {
    fun parse(arch: String): Architecture {
      return when (arch) {
        "arm" -> ARM
        "aarch64" -> AARCH64
        "x86" -> X86
        "x86_64" -> X86_64
        else -> ALL
      }
    }
  }
}
