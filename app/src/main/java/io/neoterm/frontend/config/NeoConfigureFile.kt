package io.neoterm.frontend.config

import io.neolang.parser.NeoLangParser
import io.neoterm.utils.FileUtils
import java.io.File

/**
 * @author kiva
 */
class NeoConfigureFile(val configureFile: String) {
    private val configParser = NeoLangParser()
    private var configVisitor : ConfigVisitor? = null

    fun getVisitor(): ConfigVisitor {
        checkParsed()
        return configVisitor!!
    }

    fun parseConfigure(): Boolean {
        val configContent = FileUtils.readFile(File(configureFile)) ?: return false
        val programCode = String(configContent)
        configParser.setInputSource(programCode)

        val ast = configParser.parse()
        val astVisitor = ast.visit().getVisitor(ConfigVisitor::class.java) ?: return false
        astVisitor.start()
        configVisitor = astVisitor.getCallback()
        return true
    }

    private fun checkParsed() {
        if (configVisitor == null) {
            throw IllegalStateException("Configure file not loaded.")
        }
    }
}
