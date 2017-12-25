package io.neoterm.component.codegen.interfaces

import io.neoterm.component.codegen.CodeGenParameter

/**
 * @author kiva
 */
abstract class ICodeGenerator(parameter: CodeGenParameter) {
    abstract fun getGeneratorName(): String

    abstract fun generateCode(codeGenObject: CodeGenObject): String
}
