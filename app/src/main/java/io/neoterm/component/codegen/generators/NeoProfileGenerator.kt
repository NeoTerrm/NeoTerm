package io.neoterm.component.codegen.generators

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.interfaces.ICodeGenerator
import io.neoterm.component.codegen.interfaces.CodeGenObject

/**
 * @author kiva
 */
class NeoProfileGenerator(parameter: CodeGenParameter) : ICodeGenerator(parameter) {
    override fun getGeneratorName(): String {
        return "NeoProfile-Generator"
    }

    override fun generateCode(codeGenObject: CodeGenObject): String {
        return ""
    }
}