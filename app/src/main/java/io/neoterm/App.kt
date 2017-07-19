package io.neoterm

import android.app.Application
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.customize.font.FontManager
import io.neoterm.customize.script.UserScriptManager
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
        ColorSchemeManager.init(this)
        FontManager.init(this)
        UserScriptManager.init(this)
    }

    companion object {
        private var app: App? = null

        fun get(): App {
            return app!!
        }
    }
}