package io.neoterm

import android.app.Application
import android.util.Log
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.customize.completion.CompletionProviderManager
import io.neoterm.customize.eks.ExtraKeysManager
import io.neoterm.customize.font.FontManager
import io.neoterm.customize.pm.NeoPackageManager
import io.neoterm.customize.script.UserScriptManager
import io.neoterm.frontend.service.ServiceManager
import io.neoterm.preference.NeoPreference
import io.neoterm.utils.CrashHandler

/**
 * @author kiva
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        NeoPreference.init(this)
        CrashHandler.init()

        // ensure that we can access these any time
        ServiceManager.registerService(ColorSchemeManager::class.java)
        ServiceManager.registerService(FontManager::class.java)
        ServiceManager.registerService(UserScriptManager::class.java)
        ServiceManager.registerService(ExtraKeysManager::class.java)
        ServiceManager.registerService(CompletionProviderManager::class.java)
        ServiceManager.registerService(NeoPackageManager::class.java)
    }

    companion object {
        private var app: App? = null

        fun get(): App {
            return app!!
        }
    }
}