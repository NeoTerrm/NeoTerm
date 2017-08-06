package io.neoterm

import android.app.Application
import io.neoterm.customize.NeoInitializer
import io.neoterm.frontend.preference.NeoPreference
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
        NeoInitializer.initialize(this)
    }

    companion object {
        private var app: App? = null

        fun get(): App {
            return app!!
        }
    }
}