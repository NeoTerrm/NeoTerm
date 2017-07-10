package io.neoterm.utils

import android.content.Intent
import android.os.Process
import io.neoterm.App
import io.neoterm.ui.crash.CrashActivity

/**
 * @author kiva
 */
object CrashHandler : Thread.UncaughtExceptionHandler {
    private lateinit var defaultHandler: Thread.UncaughtExceptionHandler

    fun init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val intent = Intent(App.get(), CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("exception", e)
        App.get().startActivity(intent)
        Process.killProcess(Process.myPid())
    }
}