package io.neoterm.component.codegen.interfaces

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.interfaces.ICodeGenerator

/**
 * @author kiva
 */
interface CodeGenObject {
    fun getCodeGenerator(parameter: CodeGenParameter): ICodeGenerator
}