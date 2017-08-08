package io.neoterm

import io.neoterm.customize.color.NeoColorScheme
import io.neoterm.customize.config.ConfigureService
import io.neolang.visitor.ConfigVisitor
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
        val visitor = parseConfigure("NeoLang/example/extra-key.nl")
        if (visitor != null) {
            val programs = visitor.getArray(arrayOf("extra-key"), "program")
            programs.forEachIndexed { index, element ->
                println("program[$index]: ${element.eval().asString()}")
            }

            val keys = visitor.getArray(arrayOf("extra-key"), "key")
            keys.forEachIndexed { index, element ->
                if (element.isBlock()) {
                    println("key[$index]: " +
                            "display: ${element.eval("display").asString()}, " +
                            "code: ${element.eval("code").asString()}")
                }
            }
        }
    }

    @Test
    fun configureFileTest() {
        colorConfigureTest()
        extraKeyConfigureTest()
    }
}