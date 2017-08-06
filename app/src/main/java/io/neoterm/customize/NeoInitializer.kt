package io.neoterm.customize

import android.content.Context
import io.neoterm.customize.color.ColorSchemeService
import io.neoterm.customize.completion.CompletionService
import io.neoterm.customize.config.ConfigureService
import io.neoterm.customize.eks.ExtraKeysService
import io.neoterm.customize.font.FontService
import io.neoterm.customize.pm.NeoPackageService
import io.neoterm.customize.script.UserScriptService
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.service.ServiceManager

/**
 * @author kiva
 */
object NeoInitializer {
    fun initialize(context: Context) {
        NLog.init(context)
        ServiceManager.registerService(ConfigureService::class.java)
        ServiceManager.registerService(ColorSchemeService::class.java)
        ServiceManager.registerService(FontService::class.java)
        ServiceManager.registerService(UserScriptService::class.java)
        ServiceManager.registerService(ExtraKeysService::class.java)
        ServiceManager.registerService(CompletionService::class.java)
        ServiceManager.registerService(NeoPackageService::class.java)
    }
}