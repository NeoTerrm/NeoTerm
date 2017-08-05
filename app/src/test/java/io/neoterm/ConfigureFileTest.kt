package io.neoterm

import io.neoterm.frontend.config.ConfigVisitor
import io.neoterm.frontend.config.NeoConfigureFile
import org.junit.Test

/**
 * @author kiva
 */
class ConfigureFileTest {
    private fun printAttr(visitor: ConfigVisitor, contextName: String, attrName: String) {
        println("attr [$contextName->$attrName]: ${visitor.getAttribute(contextName, attrName).asString()}")
    }

    @Test
    fun configureFileTest() {
        val config = NeoConfigureFile("NeoLang/example/color-scheme.nl")
        if (config.parseConfigure()) {
            println("Parsed!")
            val visitor = config.getVisitor()
            printAttr(visitor, "colors", "foreground")
        }
    }
}