package io.neoterm.component.pm

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

class NeoPackageInfo {
  var packageName: String? = null
  var isEssential: Boolean = false
  var version: String? = null
  var architecture: Architecture = Architecture.ALL
  var maintainer: String? = null
  var installedSizeInBytes: Long = 0L
  var fileName: String? = null
  var dependenciesString: String? = null
  var dependencies: Array<NeoPackageInfo>? = null
  var sizeInBytes: Long = 0L
  var md5: String? = null
  var sha1: String? = null
  var sha256: String? = null
  var homePage: String? = null
  var description: String? = null
}
