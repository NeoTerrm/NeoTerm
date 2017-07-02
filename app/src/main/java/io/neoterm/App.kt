package io.neoterm

import android.app.Application
import io.neoterm.customize.font.FontManager
import io.neoterm.preference.NeoPreference

/**
 * @author kiva
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FontManager.init(this)
        NeoPreference.init(this)
    }
}