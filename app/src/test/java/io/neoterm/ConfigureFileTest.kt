package io.neoterm

import io.neoterm.component.color.NeoColorScheme
import io.neoterm.component.eks.NeoExtraKey
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class ConfigureFileTest {
    @Test
    fun colorConfigureTest() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val color = NeoColorScheme()
        if (color.loadConfigure(File("NeoLang/example/color-scheme.nl"))) {
            println("colorName:    ${color.colorName}")
            println("colorVersion: ${color.colorVersion}")
            println("background:   ${color.backgroundColor}")
            println("foreground:   ${color.foregroundColor}")
            println("color1:       ${color.color[1]}")
            println("color2:       ${color.color[2]}")
        }
    }

    @Test
    fun extraKeyConfigureTest() {
        try {
            TestInitializer.init()
        } catch (ignore: Throwable) {
        }

        val extraKey = NeoExtraKey()
        if (extraKey.loadConfigure(File("/Users/kiva/Documents/NeoTerm/app/src/main/assets/eks/vim.nl"))) {
            println("programs:     ${extraKey.programNames}")
            println("version:      ${extraKey.version}")
            println("with-default: ${extraKey.withDefaultKeys}")
            println("keys:         ${extraKey.shortcutKeys}")
        }
    }

    @Test
    fun configureFileTest() {
        colorConfigureTest()
        extraKeyConfigureTest()
    }
}