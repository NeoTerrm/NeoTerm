package io.neoterm.component.codegen.interfaces

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.model.CodeGenObject

/**
 * @author kiva
 */
abstract class ICodeGenerator(parameter: CodeGenParameter) {
    abstract fun getGeneratorName(): String

    abstract fun generateCode(codeGenObject: CodeGenObject): String
}
