package io.neoterm.utils

import android.content.Context
import android.widget.Toast
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.font.FontManager
import io.neoterm.preference.NeoPreference
import io.neoterm.preference.NeoTermPath
import io.neoterm.view.BasicViewClient
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import java.io.File

/**
 * @author kiva
 */
object TerminalUtils {
    fun setupTerminalView(terminalView: TerminalView?, terminalViewClient: BasicViewClient? = null) {
        terminalView?.textSize = NeoPreference.loadInt(NeoPreference.KEY_FONT_SIZE, 30)
        terminalView?.setTypeface(FontManager.getCurrentFont().getTypeFace())
        if (terminalViewClient != null) {
            terminalView?.setTerminalViewClient(terminalViewClient)
        }
    }

    fun setupExtraKeysView(extraKeysView: ExtraKeysView?) {
        extraKeysView?.setTypeface(FontManager.getCurrentFont().getTypeFace())
    }

    fun setupTerminalSession(session: TerminalSession?) {
    }

    fun createSession(context: Context, executablePath: String?, arguments: Array<String>?,
                      cwd: String?, initialCommand: String?, env: Array<String>?,
                      sessionCallback: TerminalSession.SessionChangedCallback?,
                      systemShell: Boolean): TerminalSession {

        var executablePath = executablePath
        var arguments = arguments
        var initialCommand = initialCommand
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

        if (initialCommand == null) {
            initialCommand = NeoPreference.loadString(R.string.key_general_initial_command, "")
        }

        val session = TerminalSession(executablePath, cwd, initialCommand, arguments,
                env ?: NeoPreference.buildEnvironment(cwd, systemShell, executablePath),
                sessionCallback)
        setupTerminalSession(session)
        return session
    }

    fun escapeString(s: String?): String {
        if (s == null) {
            return ""
        }

        val builder = StringBuilder()
        val specialChars = "\"\\$`!"
        builder.append('"')
        val length = s.length
        for (i in 0..length - 1) {
            val c = s[i]
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\')
            }
            builder.append(c)
        }
        builder.append('"')
        return builder.toString()
    }
}