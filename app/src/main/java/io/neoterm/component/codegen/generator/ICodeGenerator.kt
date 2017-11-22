package com.termux.component.codegen.generator

import com.termux.component.codegen.CodeGenParameter
import com.termux.component.codegen.model.CodeGenObject

/**
 * @author Sam
 */
abstract class ICodeGenerator(parameter: CodeGenParameter) {
    abstract fun getGeneratorName(): String

    abstract fun generateCode(codeGenObject: CodeGenObject): String
}
