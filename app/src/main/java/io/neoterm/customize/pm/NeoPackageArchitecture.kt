package io.neoterm.customize.pm

/**
 * @author kiva
 */

enum class NeoPackageArchitecture {
    ALL, ARM, AARCH64, X86, X86_64;


    companion object {

        fun parse(arch: String): NeoPackageArchitecture {
            when (arch) {
                "arm" -> return ARM
                "aarch64" -> return AARCH64
                "x86" -> return X86
                "x86_64" -> return X86_64
                else -> return ALL
            }
        }
    }
}
