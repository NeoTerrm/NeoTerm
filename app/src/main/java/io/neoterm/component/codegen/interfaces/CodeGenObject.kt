package io.neoterm.component.codegen.interfaces

import io.neoterm.component.codegen.CodeGenParameter

/**
 * @author kiva
 */
interface CodeGenObject {
    fun getCodeGenerator(parameter: CodeGenParameter): CodeGenerator
}