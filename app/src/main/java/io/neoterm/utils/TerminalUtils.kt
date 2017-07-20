package io.neoterm.utils

import android.content.Context
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.font.FontManager
import io.neoterm.preference.NeoPreference
import io.neoterm.preference.NeoTermPath
import io.neoterm.terminal.ShellTermSession
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalDialog
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

/**
 * @author kiva
 */
object TerminalUtils {
    fun setupTerminalView(terminalView: TerminalView?, terminalViewClient: TerminalViewClient? = null) {
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

    fun createShellSession(context: Context, executablePath: String?, arguments: Array<String>?,
                           cwd: String?, initialCommand: String?, env: Array<Pair<String, String>>?,
                           sessionCallback: TerminalSession.SessionChangedCallback?,
                           systemShell: Boolean): TerminalSession {
        val initCommand = initialCommand ?:
                NeoPreference.loadString(R.string.key_general_initial_command, "")

        val session = ShellTermSession.Builder()
                .shell(executablePath)
                .currentWorkingDirectory(cwd)
                .callback(sessionCallback)
                .systemShell(systemShell)
                .envArray(env)
                .argArray(arguments)
                .create(context)
        setupTerminalSession(session)
        session.initialCommand = initCommand
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

    fun executeApt(context: Context, subCommand: String, callback: (Int, TerminalDialog) -> Unit) {
        TerminalDialog(context)
                .onFinish(object : TerminalDialog.SessionFinishedCallback {
                    override fun onSessionFinished(dialog: TerminalDialog, finishedSession: TerminalSession?) {
                        val exit = finishedSession?.exitStatus ?: 1
                        callback(exit, dialog)
                    }
                })
                .imeEnabled(true)
                .execute(NeoTermPath.APT_BIN_PATH, arrayOf("apt", subCommand))
                .show("apt $subCommand")
    }
}