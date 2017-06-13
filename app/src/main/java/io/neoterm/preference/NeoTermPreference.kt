package io.neoterm.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.neoterm.backend.TerminalSession
import io.neoterm.services.NeoTermService
import java.io.File


/**
 * @author kiva
 */

object NeoTermPreference {
    const val ROOT_PATH = "/data/data/io.neoterm/files"
    const val USR_PATH = ROOT_PATH + "/usr"
    const val HOME_PATH = ROOT_PATH + "/home"

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

    fun buildEnvironment(cwd: String?, systemShell: Boolean): Array<String> {
        var cwd = cwd
        File(HOME_PATH).mkdirs()

        if (cwd == null) cwd = HOME_PATH

        val termEnv = "TERM=xterm-256color"
        val homeEnv = "HOME=" + HOME_PATH
        val androidRootEnv = "ANDROID_ROOT=" + System.getenv("ANDROID_ROOT")
        val androidDataEnv = "ANDROID_DATA=" + System.getenv("ANDROID_DATA")
        val externalStorageEnv = "EXTERNAL_STORAGE=" + System.getenv("EXTERNAL_STORAGE")

        if (systemShell) {
            val pathEnv = "PATH=" + System.getenv("PATH")
            return arrayOf(termEnv, homeEnv, androidRootEnv, androidDataEnv, externalStorageEnv, pathEnv)

        } else {
            val ps1Env = "PS1=$ "
            val ldEnv = "LD_LIBRARY_PATH=$USR_PATH/lib"
            val langEnv = "LANG=en_US.UTF-8"
            val pathEnv = "PATH=$USR_PATH/bin:$USR_PATH/bin/applets"
            val pwdEnv = "PWD=" + cwd
            val tmpdirEnv = "TMPDIR=$USR_PATH/tmp"

            return arrayOf(termEnv, homeEnv, ps1Env, ldEnv, langEnv, pathEnv, pwdEnv, androidRootEnv, androidDataEnv, externalStorageEnv, tmpdirEnv)
        }
    }
}
