package io.neoterm.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.neoterm.backend.TerminalSession
import io.neoterm.services.NeoTermService

/**
 * @author kiva
 */

object NeoTermPreference {
    const val KEY_FONT_SIZE = "neoterm_general_font_size"
    const val KEY_CURRENT_SESSION = "neoterm_service_current_session"

    var preference: SharedPreferences? = null
    var context: Context? = null

    fun init(context: Context) {
        this.context = context
        preference = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun cleanup() {
        preference = null
        context = null
    }

    fun store(key: Int, value: Any) {
        store(context!!.getString(key), value)
    }

    fun store(key: String, value: Any) {
        when (value) {
            is Int -> preference!!.edit().putInt(key, value).apply()
            is String -> preference!!.edit().putString(key, value).apply()
            is Boolean -> preference!!.edit().putBoolean(key, value).apply()
        }
    }

    fun loadInt(key: Int, defaultValue: Int): Int {
        return loadInt(context!!.getString(key), defaultValue)
    }

    fun loadString(key: Int, defaultValue: String?): String {
        return loadString(context!!.getString(key), defaultValue)
    }

    fun loadBoolean(key: Int, defaultValue: Boolean): Boolean {
        return loadBoolean(context!!.getString(key), defaultValue)
    }

    fun loadInt(key: String, defaultValue: Int): Int {
        return preference!!.getInt(key, defaultValue)
    }

    fun loadString(key: String, defaultValue: String?): String {
        return preference!!.getString(key, defaultValue)
    }

    fun loadBoolean(key: String, defaultValue: Boolean): Boolean {
        return preference!!.getBoolean(key, defaultValue)
    }

    fun storeCurrentSession(session: TerminalSession) {
        preference!!.edit()
                .putString(NeoTermPreference.KEY_CURRENT_SESSION, session.mHandle)
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
}
