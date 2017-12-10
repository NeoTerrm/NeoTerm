package io.neoterm.component.codegen.impl

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.generator.ICodeGenerator
import io.neoterm.component.codegen.model.CodeGenObject

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