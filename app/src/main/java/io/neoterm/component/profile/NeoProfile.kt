package io.neoterm.component.profile

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.generator.ICodeGenerator
import io.neoterm.component.codegen.impl.NeoProfileGenerator
import io.neoterm.component.codegen.model.CodeGenObject

/**
 * @author kiva
 */
abstract class NeoProfile : CodeGenObject {
    abstract val profileMetaName: String

    abstract fun onProfileLoaded(visitor: ConfigVisitor): Boolean

    override fun getCodeGenerator(parameter: CodeGenParameter): ICodeGenerator {
        return NeoProfileGenerator(parameter)
    }
}