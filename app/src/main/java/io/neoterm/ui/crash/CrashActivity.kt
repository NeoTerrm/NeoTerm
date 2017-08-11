package io.neoterm.ui.crash

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import io.neoterm.R
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * @author kiva
 */
class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_crash)
        setSupportActionBar(findViewById<Toolbar>(R.id.crash_toolbar))

        (findViewById<TextView>(R.id.crash_model)).text = getString(R.string.crash_model, collectModelInfo())
        (findViewById<TextView>(R.id.crash_app_version)).text = getString(R.string.crash_app, collectAppInfo())
        (findViewById<TextView>(R.id.crash_details)).text = collectExceptionInfo()
    }

    private fun collectExceptionInfo(): String {
        val extra = intent.getSerializableExtra("exception")
        if (extra != null && extra is Throwable) {
            val byteArrayOutput = ByteArrayOutputStream()
            val printStream = PrintStream(byteArrayOutput)
            (extra.cause ?: extra).printStackTrace(printStream)
            return byteArrayOutput.use {
                byteArrayOutput.toString("utf-8")
            }
        }
        return "are.you.kidding.me.NoExceptionFoundException: This is a bug, please contact developers!"
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