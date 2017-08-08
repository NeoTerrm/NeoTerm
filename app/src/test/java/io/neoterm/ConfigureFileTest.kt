package io.neoterm

import io.neoterm.frontend.config.ConfigVisitor
import io.neoterm.frontend.config.NeoConfigureFile
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class ConfigureFileTest {
    private fun printAttr(visitor: ConfigVisitor, contextName: String, attrName: String) {
        println("attr [$contextName->$attrName]: ${visitor.getAttribute(contextName, attrName).asString()}")
    }

    private fun parseConfigure(filePath: String, contextName: String, attrName: String) {
        val config = NeoConfigureFile(File(filePath))
        if (config.parseConfigure()) {
            val visitor = config.getVisitor()
            printAttr(visitor, contextName, attrName)
        }
    }

    @Test
    fun configureFileTest() {
//        parseConfigure("NeoLang/example/color-scheme.nl", "colors", "foreground")
        parseConfigure("NeoLang/example/extra-key.nl", "key", "0")
    }
}