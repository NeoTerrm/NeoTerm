package io.neoterm.component.profile

import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.interfaces.CodeGenerator
import io.neoterm.component.codegen.generators.NeoProfileGenerator
import io.neoterm.component.codegen.interfaces.CodeGenObject
import io.neoterm.frontend.component.helper.ConfigFileBasedObject

/**
 * @author kiva
 */
abstract class NeoProfile : CodeGenObject, ConfigFileBasedObject {
    abstract val profileMetaName: String

    override fun getCodeGenerator(parameter: CodeGenParameter): CodeGenerator {
        return NeoProfileGenerator(parameter)
    }
}