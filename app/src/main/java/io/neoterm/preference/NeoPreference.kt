package io.neoterm.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.neoterm.App
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.services.NeoTermService
import io.neoterm.utils.FileUtils
import java.io.File


/**
 * @author kiva
 */

object NeoPreference {
    const val KEY_HAPPY_EGG = "neoterm_fun_happy"
    const val KEY_FONT_SIZE = "neoterm_general_font_size"
    const val KEY_CURRENT_SESSION = "neoterm_service_current_session"

    const val VALUE_HAPPY_EGG_TRIGGER = 8
    const val VALUE_NEOTERM_ONLY = "NeoTermOnly"
    const val VALUE_NEOTERM_FIRST = "NeoTermFirst"
    const val VALUE_SYSTEM_FIRST = "SystemFirst"

    private var preference: SharedPreferences? = null

    fun init(context: Context) {
        preference = PreferenceManager.getDefaultSharedPreferences(context)

        // load apt source
        val sourceFile = File(NeoTermPath.SOURCE_FILE)
        val bytes = FileUtils.readFile(sourceFile)
        if (bytes != null) {
            val source = String(FileUtils.readFile(sourceFile)!!).trim().trimEnd()
            val array = source.split(" ")
            if (array.size >= 2 && array[0] == "deb") {
                store(R.string.key_package_source, array[1])
            }
        }
    }

    fun store(key: Int, value: Any) {
        store(App.get().getString(key), value)
    }

    fun store(key: String, value: Any) {
        when (value) {
            is Int -> preference!!.edit().putInt(key, value).apply()
            is String -> preference!!.edit().putString(key, value).apply()
            is Boolean -> preference!!.edit().putBoolean(key, value).apply()
        }
    }

    fun loadInt(key: Int, defaultValue: Int): Int {
        return loadInt(App.get().getString(key), defaultValue)
    }

    fun loadString(key: Int, defaultValue: String?): String {
        return loadString(App.get().getString(key), defaultValue)
    }

    fun loadBoolean(key: Int, defaultValue: Boolean): Boolean {
        return loadBoolean(App.get().getString(key), defaultValue)
    }

    fun loadInt(key: String?, defaultValue: Int): Int {
        return preference!!.getInt(key, defaultValue)
    }

    fun loadString(key: String?, defaultValue: String?): String {
        return preference!!.getString(key, defaultValue)
    }

    fun loadBoolean(key: String?, defaultValue: Boolean): Boolean {
        return preference!!.getBoolean(key, defaultValue)
    }

    fun storeCurrentSession(session: TerminalSession) {
        preference!!.edit()
                .putString(NeoPreference.KEY_CURRENT_SESSION, session.mHandle)
                .apply()
    }

    fun getCurrentSession(termService: NeoTermService?): TerminalSession? {
        val sessionHandle = PreferenceManager.getDefaultSharedPreferences(termService!!).getString(KEY_CURRENT_SESSION, "")
        var i = 0
        val len = termService.sessions.size
        while (i < len) {
            val session = termService.sessions[i]
            if (session.mHandle == sessionHandle) return session
            i++
        }
        return null
    }

    /**
     * TODO
     * To print the job name about to be executed in bash:
     * $ trap 'echo -ne "\e]0;${BASH_COMMAND%% *}\x07"' DEBUG
     * $ PS1='$(echo -ne "\e]0;$PWD\x07")\$ '
     */
}
