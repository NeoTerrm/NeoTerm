package io.neoterm.component.profile

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.codegen.CodeGenParameter
import io.neoterm.component.codegen.generators.NeoProfileGenerator
import io.neoterm.component.codegen.interfaces.CodeGenObject
import io.neoterm.component.codegen.interfaces.CodeGenerator
import io.neoterm.frontend.component.helper.ConfigFileBasedObject

/**
 * @author kiva
 */
abstract class NeoProfile : CodeGenObject, ConfigFileBasedObject {
    companion object {
        private const val PROFILE_NAME = "name"
    }

    abstract val profileMetaName: String
    private val profileMetaPath
        get() = arrayOf(profileMetaName)

    var profileName = "Unknown Profile"

    override fun onConfigLoaded(configVisitor: ConfigVisitor) {
        profileName = configVisitor.getProfileString(PROFILE_NAME, profileName)
    }

    override fun getCodeGenerator(parameter: CodeGenParameter): CodeGenerator {
        return NeoProfileGenerator(parameter)
    }

    protected fun ConfigVisitor.getProfileString(key: String, fallback: String): String {
        return getProfileString(key) ?: fallback
    }

    protected fun ConfigVisitor.getProfileBoolean(key: String, fallback: Boolean): Boolean {
        return getProfileBoolean(key) ?: fallback
    }

    protected fun ConfigVisitor.getProfileString(key: String): String? {
        return this.getStringValue(profileMetaPath, key)
    }

    protected fun ConfigVisitor.getProfileBoolean(key: String): Boolean? {
        return this.getBooleanValue(profileMetaPath, key)
    }
}