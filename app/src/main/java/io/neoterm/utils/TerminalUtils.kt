package io.neoterm.utils

import android.content.Context
import android.widget.Toast
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.NeoTermPath
import io.neoterm.preference.NeoPreference
import java.io.File

/**
 * @author kiva
 */
object TerminalUtils {
    fun createSession(context: Context, executablePath: String?, arguments: Array<String>?, cwd: String?, env: Array<String>?, sessionCallback: TerminalSession.SessionChangedCallback?, systemShell: Boolean): TerminalSession {
        var executablePath = executablePath
        var arguments = arguments

        var cwd = cwd
        if (cwd == null) {
            cwd = NeoTermPath.HOME_PATH
        }

        if (executablePath == null) {
            executablePath = if (systemShell)
                "/system/bin/sh"
            else
                NeoTermPath.USR_PATH + "/bin/" + NeoPreference.loadString(R.string.key_general_shell, "sh")

            if (!File(executablePath).exists()) {
                Toast.makeText(context, context.getString(R.string.shell_not_found, executablePath), Toast.LENGTH_LONG).show()
                executablePath = NeoTermPath.USR_PATH + "/bin/sh"
            }
        }

        if (arguments == null) {
            arguments = arrayOf<String>(executablePath)
        }

        val session = TerminalSession(executablePath, cwd, arguments,
                env ?: NeoPreference.buildEnvironment(cwd, systemShell, executablePath),
                sessionCallback)
        return session
    }
}