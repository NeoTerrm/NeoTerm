package com.termux.component.pm

/**
 * @author Sam
 */

enum class Architecture {
    ALL, ARM, AARCH64, X86, X86_64;


    companion object {

        fun parse(arch: String): Architecture {
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
