package io.neoterm.component.codegen.generators

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.interfaces.CodeGenObject
import io.neoterm.component.codegen.interfaces.CodeGenerator
import io.neoterm.component.colorscheme.NeoColorScheme
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.frontend.component.ComponentManager

/**
 * @author kiva
 */
class NeoColorGenerator(parameter: CodeGenParameter) : CodeGenerator(parameter) {
    override fun getGeneratorName(): String {
        return "NeoColorScheme-Generator"
    }

    override fun generateCode(codeGenObject: CodeGenObject): String {
        if (codeGenObject !is NeoColorScheme) {
            throw RuntimeException("Invalid object type, expected NeoColorScheme, got ${codeGenObject.javaClass.simpleName}")
        }

        return buildString {
            start(this)
            generateMetaData(this, codeGenObject)
            generateColors(this, codeGenObject)
            end(this)
        }
    }

    private fun start(builder: StringBuilder) {
        builder.append("${NeoColorScheme.CONTEXT_META_NAME}: {\n")
    }

    private fun end(builder: StringBuilder) {
        builder.append("}\n")
    }

    private fun generateMetaData(builder: StringBuilder, colorScheme: NeoColorScheme) {
        val component = ComponentManager.getComponent<ConfigureComponent>()

        builder.append("    ${NeoColorScheme.COLOR_META_NAME}: \"${colorScheme.colorName}\"\n")
        builder.append("    ${NeoColorScheme.COLOR_META_VERSION}: ${colorScheme.colorVersion
                ?: component.getLoaderVersion()}\n")
        builder.append("\n")
    }

    private fun generateColors(builder: StringBuilder, colorScheme: NeoColorScheme) {
        builder.append("    ${NeoColorScheme.CONTEXT_COLOR_NAME}: {\n")

        builder.append("        ${NeoColorScheme.COLOR_DEF_BACKGROUND}: ${colorScheme.backgroundColor}\n")
        builder.append("        ${NeoColorScheme.COLOR_DEF_FOREGROUND}: ${colorScheme.foregroundColor}\n")
        builder.append("        ${NeoColorScheme.COLOR_DEF_CURSOR}: ${colorScheme.cursorColor}\n")
        colorScheme.color.entries.forEach {
            builder.append("        ${NeoColorScheme.COLOR_PREFIX}${it.key}: ${it.value}\n")
        }

        builder.append("    }\n")
    }
}