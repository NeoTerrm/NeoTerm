package io.neoterm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import io.neoterm.component.NeoInitializer
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.ui.bonus.BonusActivity
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
        NeoInitializer.init(this)
    }

    fun errorDialog(context: Context, message: Int, dismissCallback: (() -> Unit)?) {
        errorDialog(context, getString(message), dismissCallback)
    }

    fun errorDialog(context: Context, message: String, dismissCallback: (() -> Unit)?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(message)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(R.string.show_help, { _, _ ->
                    openHelpLink()
                })
                .setOnDismissListener {
                    dismissCallback?.invoke()
                }
                .show()
    }

    fun openHelpLink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://neoterm.gitbooks.io/neoterm-wiki/content/"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun easterEgg(context: Context, message: String) {
        val happyCount = NeoPreference.loadInt(NeoPreference.KEY_HAPPY_EGG, 0) + 1
        NeoPreference.store(NeoPreference.KEY_HAPPY_EGG, happyCount)

        val trigger = NeoPreference.VALUE_HAPPY_EGG_TRIGGER

        if (happyCount == trigger / 2) {
            @SuppressLint("ShowToast")
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        } else if (happyCount > trigger) {
            NeoPreference.store(NeoPreference.KEY_HAPPY_EGG, 0)
            context.startActivity(Intent(context, BonusActivity::class.java))
        }
    }

    companion object {
        private var app: App? = null

        fun get(): App {
            return app!!
        }

    }
}