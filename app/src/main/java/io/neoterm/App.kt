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
        ColorSchemeManager.init(this)
        FontManager.init(this)
    }
}