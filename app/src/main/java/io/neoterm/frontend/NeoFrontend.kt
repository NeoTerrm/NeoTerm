package io.neoterm.frontend

import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.customize.completion.CompletionProviderManager
import io.neoterm.customize.eks.ExtraKeysManager
import io.neoterm.customize.font.FontManager
import io.neoterm.customize.pm.NeoPackageManager
import io.neoterm.customize.script.UserScriptManager
import io.neoterm.frontend.logger.NLog
import io.neoterm.frontend.service.ServiceManager

/**
 * @author kiva
 */
object NeoFrontend {
    fun initialize() {
        // ensure that we can access these any time
        ServiceManager.registerService(ColorSchemeManager::class.java)
        ServiceManager.registerService(FontManager::class.java)
        ServiceManager.registerService(UserScriptManager::class.java)
        ServiceManager.registerService(ExtraKeysManager::class.java)
        ServiceManager.registerService(CompletionProviderManager::class.java)
        ServiceManager.registerService(NeoPackageManager::class.java)

        NLog.initialize()
    }
}