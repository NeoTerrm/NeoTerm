package io.neoterm

import android.app.Application
import io.neoterm.frontend.NeoFrontend
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
        NeoFrontend.initialize()
    }

    companion object {
        private var app: App? = null

        fun get(): App {
            return app!!
        }
    }
}