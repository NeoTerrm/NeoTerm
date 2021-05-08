package io.neoterm.component.codegen

abstract class CodeGenerator(parameter: CodeGenParameter) {
  abstract fun getGeneratorName(): String
  abstract fun generateCode(codeGenObject: CodeGenObject): String
}

interface CodeGenObject {
  fun getCodeGenerator(parameter: CodeGenParameter): CodeGenerator
}
