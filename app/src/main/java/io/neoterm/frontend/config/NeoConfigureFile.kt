package io.neoterm.frontend.config

import io.neolang.parser.NeoLangParser
import io.neolang.visitor.ConfigVisitor
import java.io.File
import java.nio.file.Files

/**
 * @author kiva
 */
open class NeoConfigureFile(val configureFile: File) {
    private val configParser = NeoLangParser()
    open protected var configVisitor : ConfigVisitor? = null

    fun getVisitor() = configVisitor ?: throw IllegalStateException("Configure file not loaded or parse failed.")

    open fun parseConfigure() = kotlin.runCatching {
        val programCode = String(Files.readAllBytes(configureFile.toPath()))
        configParser.setInputSource(programCode)

        val ast = configParser.parse()
        val astVisitor = ast.visit().getVisitor(ConfigVisitor::class.java) ?: return false
        astVisitor.start()
        configVisitor = astVisitor.getCallback()
    }.isSuccess
}
