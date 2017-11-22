package com.termux.component.codegen.model

import com.termux.component.codegen.CodeGenParameter
import com.termux.component.codegen.generator.ICodeGenerator

/**
 * @author Sam
 */
interface CodeGenObject {
    fun getCodeGenerator(parameter: CodeGenParameter): ICodeGenerator
}
