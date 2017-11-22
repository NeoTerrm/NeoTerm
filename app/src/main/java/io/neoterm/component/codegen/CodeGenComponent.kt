package com.termux.component.codegen

import com.termux.component.codegen.generator.ICodeGenerator
import com.termux.component.codegen.model.CodeGenObject
import com.termux.frontend.component.NeoComponent

/**
 * @author Sam
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
        return codeObject.getCodeGenerator(parameter)
    }
}

