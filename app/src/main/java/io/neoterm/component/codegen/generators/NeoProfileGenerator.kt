package io.neoterm.component.codegen.generators

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.interfaces.CodeGenObject
import io.neoterm.component.codegen.interfaces.CodeGenerator

/**
 * @author kiva
 */
class NeoProfileGenerator(parameter: CodeGenParameter) : CodeGenerator(parameter) {
  override fun getGeneratorName(): String {
    return "NeoProfile-Generator"
  }

  override fun generateCode(codeGenObject: CodeGenObject): String {
    return ""
  }
}