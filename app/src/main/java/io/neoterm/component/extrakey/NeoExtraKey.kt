package io.neoterm.component.extrakey

import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.config.ConfigureComponent
import io.neoterm.frontend.config.NeoConfigureFile
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
import io.neoterm.frontend.terminal.extrakey.button.IExtraButton
import io.neoterm.frontend.terminal.extrakey.button.TextButton
import java.io.File

/**
 * @author kiva
 */
class NeoExtraKey {
    companion object {
        const val EKS_META_CONTEXT_NAME = "extra-key"

        const val EKS_META_PROGRAM = "program"
        const val EKS_META_KEY = "key"
        const val EKS_META_WITH_DEFAULT = "with-default"
        const val EKS_META_WITH_ENTER = "with-enter"
        const val EKS_META_DISPLAY = "display"
        const val EKS_META_CODE = "code"
        const val EKS_META_VERSION = "version"

        val EKS_META_CONTEXT_PATH = arrayOf(EKS_META_CONTEXT_NAME)
    }

    var version: Int = 0
    val programNames: MutableList<String> = mutableListOf()
    val shortcutKeys: MutableList<IExtraButton> = mutableListOf()
    var withDefaultKeys: Boolean = true

    fun applyExtraKeys(extraKeysView: ExtraKeysView) {
        if (withDefaultKeys) {
            extraKeysView.loadDefaultUserKeys()
        }
        for (button in shortcutKeys) {
            extraKeysView.addUserKey(button)
        }
    }

    fun loadConfigure(file: File): Boolean {
        // TODO: Refactor with NeoColorScheme#onConfigLoaded
        val loaderService = ComponentManager.getComponent<ConfigureComponent>()

        val configure: NeoConfigureFile?
        try {
            configure = loaderService.newLoader(file).loadConfigure()
            if (configure == null) {
                throw RuntimeException("Parse configuration failed.")
            }
        } catch (e: Exception) {
            NLog.e("ExtraKey", "Failed to load extra key config: ${file.absolutePath}: ${e.localizedMessage}")
            return false
        }

        val visitor = configure.getVisitor()

        // program
        val programArray = visitor.getArray(EKS_META_CONTEXT_PATH, EKS_META_PROGRAM)
        if (programArray.isEmpty()) {
            NLog.e("ExtraKey", "Failed to load extra key config: ${file.absolutePath}: Extra Key must have programs attribute")
            return false
        }

        programArray.forEach {
            if (!it.isBlock()) {
                programNames.add(it.eval().asString())
            }
        }

        // key
        val keyArray = visitor.getArray(EKS_META_CONTEXT_PATH, EKS_META_KEY)
        keyArray.takeWhile { it.isBlock() }
                .forEach {
                    val display = it.eval(EKS_META_DISPLAY)
                    val code = it.eval(EKS_META_CODE)
                    if (!code.isValid()) {
                        NLog.e("ExtraKey", "Failed to load extra key config: ${file.absolutePath}: Key must have a code")
                        return false
                    }

                    val codeText = code.asString()
                    val displayText = if (display.isValid()) display.asString() else codeText
                    val withEnter = it.eval(EKS_META_WITH_ENTER)
                    val withEnterBoolean = withEnter.asString() == "true"

                    val button = TextButton(codeText, withEnterBoolean)
                    button.displayText = displayText
                    shortcutKeys.add(button)
                }

        // We must cal toDouble() before toInt()
        // Because in NeoLang, numbers are default to Double
        version = getMetaByVisitor(visitor, EKS_META_VERSION)?.toDouble()?.toInt() ?: 0
        withDefaultKeys = "true" == getMetaByVisitor(visitor, EKS_META_WITH_DEFAULT)
        return true
    }

    private fun getMetaByVisitor(visitor: ConfigVisitor, metaName: String): String? {
        return visitor.getStringValue(EKS_META_CONTEXT_PATH, metaName)
    }
}