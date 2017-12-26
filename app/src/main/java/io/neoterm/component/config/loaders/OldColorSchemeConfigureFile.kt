package io.neoterm.component.config.loaders

import io.neolang.runtime.type.NeoLangValue
import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.colorscheme.NeoColorScheme
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.logging.NLog
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * @author kiva
 */
class OldColorSchemeConfigureFile(configureFile: File) : NeoConfigureFile(configureFile) {
    override var configVisitor: ConfigVisitor? = null

    override fun parseConfigure(): Boolean {
        try {
            val visitor = ConfigVisitor()
            visitor.onStart()
            visitor.onEnterContext(NeoColorScheme.CONTEXT_META_NAME)

            visitor.getCurrentContext()
                    .defineAttribute(NeoColorScheme.COLOR_META_NAME, NeoLangValue(configureFile.nameWithoutExtension))
                    .defineAttribute(NeoColorScheme.COLOR_META_VERSION, NeoLangValue("1.0"))

            visitor.onEnterContext(NeoColorScheme.CONTEXT_COLOR_NAME)

            return FileInputStream(configureFile).use {
                val prop = Properties()
                prop.load(it)
                prop.forEach {
                    visitor.getCurrentContext().defineAttribute(it.key as String, NeoLangValue(it.value as String))
                }
                visitor.onFinish()
                this.configVisitor = visitor
                true
            }

        } catch (e: Exception) {
            this.configVisitor = null
            NLog.e("ConfigureLoader", "Error while loading old config", e)
            return false
        }
    }
}