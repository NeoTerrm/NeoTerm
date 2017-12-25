package io.neoterm.component.codegen.interfaces

import io.neoterm.component.codegen.CodeGenParameter

/**
 * @author kiva
 */
abstract class CodeGenerator(parameter: CodeGenParameter) {
    abstract fun getGeneratorName(): String

    abstract fun generateCode(codeGenObject: CodeGenObject): String
}
