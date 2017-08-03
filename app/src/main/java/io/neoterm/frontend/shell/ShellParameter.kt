package io.neoterm.frontend.shell

import io.neoterm.backend.TerminalSession

/**
 * @author kiva
 */
class ShellParameter {
    var executablePath: String? = null
    var arguments: Array<String>? = null
    var cwd: String? = null
    var initialCommand: String? = null
    var env: Array<Pair<String, String>>? = null
    var sessionCallback: TerminalSession.SessionChangedCallback? = null
    var systemShell: Boolean = false

    fun executablePath(executablePath: String?): ShellParameter {
        this.executablePath = executablePath
        return this
    }

    fun arguments(arguments: Array<String>?): ShellParameter {
        this.arguments = arguments
        return this
    }

    fun currentWorkingDirectory(cwd: String?): ShellParameter {
        this.cwd = cwd
        return this
    }

    fun initialCommand(initialCommand: String?): ShellParameter {
        this.initialCommand = initialCommand
        return this
    }

    fun environment(env: Array<Pair<String, String>>?): ShellParameter {
        this.env = env
        return this
    }

    fun callback(callback: TerminalSession.SessionChangedCallback?): ShellParameter {
        this.sessionCallback = callback
        return this
    }

    fun systemShell(systemShell: Boolean): ShellParameter {
        this.systemShell = systemShell
        return this
    }
}