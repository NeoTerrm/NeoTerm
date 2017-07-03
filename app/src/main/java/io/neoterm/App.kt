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
        // ensure that we can access these any time
        ColorSchemeManager.init(this)
        FontManager.init(this)
    }
}