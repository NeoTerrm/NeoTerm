package io.neoterm

import android.app.Application
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.customize.font.FontManager

/**
 * @author kiva
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        // ensure that we can access these any time
        ColorSchemeManager.init(this)
        FontManager.init(this)
    }

    companion object {
        var app: App? = null

        fun get(): App {
            return app!!
        }
    }
}