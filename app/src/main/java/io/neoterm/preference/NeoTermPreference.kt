package io.neoterm.preference

import android.content.Context
import android.preference.PreferenceManager

import io.neoterm.NeoTermActivity
import io.neoterm.terminal.TerminalSession

/**
 * @author kiva
 */

object NeoTermPreference {
    var CURRENT_SESSION_KEY = "neoterm_current_session"

    fun storeCurrentSession(context: Context, session: TerminalSession) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(NeoTermPreference.CURRENT_SESSION_KEY, session.mHandle).apply()
    }

    fun getCurrentSession(termActivity: NeoTermActivity): TerminalSession? {
        val sessionHandle = PreferenceManager.getDefaultSharedPreferences(termActivity).getString(CURRENT_SESSION_KEY, "")
        var i = 0
        val len = termActivity.termService!!.sessions.size
        while (i < len) {
            val session = termActivity.termService!!.sessions[i]
            if (session.mHandle == sessionHandle) return session
            i++
        }
        return null
    }
}
