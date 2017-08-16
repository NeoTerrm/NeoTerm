package io.neoterm.component.codegen

import io.neoterm.component.codegen.generator.ICodeGenerator
import io.neoterm.component.codegen.model.CodeGenObject
import io.neoterm.frontend.component.NeoComponent

/**
 * @author kiva
 */
class CodeGenComponent : NeoComponent {
    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }

    fun newGenerator(codeObject: CodeGenObject): ICodeGenerator {
        val parameter = CodeGenParameter()
        val generator = codeObject.getCodeGenerator(parameter)
        return generator
    }
}

