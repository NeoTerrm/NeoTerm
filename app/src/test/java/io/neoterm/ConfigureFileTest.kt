package io.neoterm

import io.neolang.visitor.ConfigVisitor
import io.neoterm.customize.color.NeoColorScheme
import io.neoterm.customize.config.ConfigureService
import io.neoterm.customize.eks.NeoExtraKey
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.service.ServiceManager
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class ConfigureFileTest {
    private fun parseConfigure(filePath: String): ConfigVisitor? {
        val config = NeoConfigureFile(File(filePath))
        if (config.parseConfigure()) {
            val visitor = config.getVisitor()
            return visitor
        }
        return null
    }

    @Test
    fun colorConfigureTest() {
        try {
            ServiceManager.registerService(ConfigureService::class.java)
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
            ServiceManager.registerService(ConfigureService::class.java)
        } catch (ignore: Throwable) {
        }

        val extraKey = NeoExtraKey()
        if (extraKey.loadConfigure(File("NeoLang/example/extra-key.nl"))) {
            println("programs:    ${extraKey.programNames}")
            println("version:     ${extraKey.version}")
            println("with-default:${extraKey.withDefaultKeys}")
            println("keys:        ${extraKey.shortcutKeys}")
        }
    }

    @Test
    fun configureFileTest() {
        colorConfigureTest()
        extraKeyConfigureTest()
    }
}