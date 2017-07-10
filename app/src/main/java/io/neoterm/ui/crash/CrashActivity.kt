package io.neoterm.ui.crash

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import io.neoterm.R

/**
 * @author kiva
 */
class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_crash)
        setSupportActionBar(findViewById(R.id.crash_toolbar) as Toolbar)

        (findViewById(R.id.crash_model) as TextView).text = getString(R.string.crash_model, collectModelInfo())
        (findViewById(R.id.crash_app_version) as TextView).text = getString(R.string.crash_app, collectAppInfo())
        (findViewById(R.id.crash_details) as TextView).text = collectExceptionInfo()
    }

    private fun collectExceptionInfo(): String {
        val extra = intent.getSerializableExtra("exception")
        if (extra != null && extra is Throwable) {
            val s = StringBuilder(extra.toString() + "\n\n")
            for ((index, trace) in extra.stackTrace.withIndex()) {
                s.append(String.format("  #%02d  ", index))
                s.append("${trace.className}(${trace.fileName}:${trace.lineNumber})(native=${trace.isNativeMethod})")
                return s.toString()
            }
        }
        return "are.you.kidding.me.NoExceptionFoundException: This is a bug, please contact me!"
    }

    fun collectAppInfo(): String {
        val pm = packageManager
        val info = pm.getPackageInfo(packageName, 0)
        return "${info.versionName} (${info.versionCode})"
    }

    private fun collectModelInfo(): String {
        return "${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
    }
}