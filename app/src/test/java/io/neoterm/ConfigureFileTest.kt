package io.neoterm

import io.neoterm.customize.color.NeoColorScheme
import io.neoterm.frontend.config.ConfigVisitor
import io.neoterm.frontend.config.NeoConfigureFile
import org.junit.Test
import java.io.File

/**
 * @author kiva
 */
class ConfigureFileTest {
    private fun getMetaByVisitor(visitor: ConfigVisitor, metaName: String): String? {
        val value = visitor.getAttribute(NeoColorScheme.COLOR_META_PATH, metaName)
        return if (value.isValid()) value.asString() else null
    }

    private fun getColorByVisitor(visitor: ConfigVisitor, colorName: String): String? {
        val value = visitor.getAttribute(NeoColorScheme.COLOR_PATH, colorName)
        return if (value.isValid()) value.asString() else null
    }

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
        val visitor = parseConfigure("NeoLang/example/color-scheme.nl")
        if (visitor != null) {
            println("colorName:    ${getMetaByVisitor(visitor, NeoColorScheme.COLOR_META_NAME)}")
            println("colorVersion: ${getMetaByVisitor(visitor, NeoColorScheme.COLOR_META_VERSION)}")
            println("background:   ${getColorByVisitor(visitor, "background")}")
            println("foreground:   ${getColorByVisitor(visitor, "foreground")}")
        }
    }

    @Test
    fun extraKeyConfigureTest() {
        parseConfigure("NeoLang/example/extra-key.nl")
    }

    @Test
    fun configureFileTest() {
        colorConfigureTest()
        extraKeyConfigureTest()
    }
}