package io.neoterm.backend

import android.content.Context
import android.widget.Toast
import io.neoterm.R
import io.neoterm.preference.NeoPreference
import io.neoterm.preference.NeoTermPath
import io.neoterm.ui.term.tab.TermSessionCallback
import java.io.File

/**
 * @author kiva
 */
open class ShellTermSession : TerminalSession {
    class Builder {
        private var shell: String? = null
        private var cwd: String? = null
        private var args: MutableList<String>? = null
        private var env: MutableList<Pair<String, String>>? = null
        private var changeCallback: SessionChangedCallback? = null
        private var systemShell = false

        fun shell(shell: String?): Builder {
            this.shell = shell
            return this
        }

        fun currentWorkingDirectory(cwd: String?): Builder {
            this.cwd = cwd
            return this
        }

        fun arg(arg: String?): Builder {
            if (arg != null) {
                if (args == null) {
                    args = mutableListOf(arg)
                } else {
                    args!!.add(arg)
                }
            }
            return this
        }

        fun args(vararg args: String?): Builder {
            if (args.isEmpty()) {
                this.args = null
                return this
            }
            args.forEach { arg(it) }
            return this
        }

        fun env(env: Pair<String, String>): Builder {
            if (this.env == null) {
                this.env = mutableListOf(env)
            } else {
                this.env!!.add(env)
            }
            return this
        }

        fun envs(vararg env: Pair<String, String>): Builder {
            if (env.isEmpty()) {
                this.env = null
                return this
            }
            env.forEach { env(it) }
            return this
        }

        fun callback(callback: SessionChangedCallback): Builder {
            this.changeCallback = callback
            return this
        }

        fun systemShell(systemShell: Boolean): Builder {
            this.systemShell = systemShell
            return this
        }

        fun create(context: Context): ShellTermSession {
            val cwd = this.cwd ?: NeoTermPath.HOME_PATH

            var shell = this.shell ?:
                    if (systemShell)
                        "/system/bin/sh"
                    else
                        NeoTermPath.USR_PATH + "/bin/" + NeoPreference.loadString(R.string.key_general_shell, "sh")

            if (!File(shell).exists()) {
                Toast.makeText(context, context.getString(R.string.shell_not_found, shell), Toast.LENGTH_LONG).show()
                shell = NeoTermPath.USR_PATH + "/bin/sh"
            }

            val args = this.args ?: mutableListOf(shell)
            val env = transformEnvironment(this.env) ?: buildEnvironment(cwd, systemShell, shell)
            val callback = changeCallback ?: TermSessionCallback()
            return ShellTermSession(shell, cwd, args.toTypedArray(), env, callback)
        }

        private fun transformEnvironment(env: MutableList<Pair<String, String>>?): Array<String>? {
            if (env == null) {
                return null
            }

            val result = mutableListOf<String>()
            env.mapTo(result, { "${it.first}=${it.second}" })
            return result.toTypedArray()
        }


        private fun buildEnvironment(cwd: String?, systemShell: Boolean, executablePath: String): Array<String> {
            var cwd = cwd
            File(NeoTermPath.HOME_PATH).mkdirs()

            if (cwd == null) cwd = NeoTermPath.HOME_PATH

            val termEnv = "TERM=xterm-256color"
            val homeEnv = "HOME=" + NeoTermPath.HOME_PATH
            val androidRootEnv = "ANDROID_ROOT=" + System.getenv("ANDROID_ROOT")
            val androidDataEnv = "ANDROID_DATA=" + System.getenv("ANDROID_DATA")
            val externalStorageEnv = "EXTERNAL_STORAGE=" + System.getenv("EXTERNAL_STORAGE")

            if (systemShell) {
                val pathEnv = "PATH=" + System.getenv("PATH")
                return arrayOf(termEnv, homeEnv, androidRootEnv, androidDataEnv, externalStorageEnv, pathEnv)

            } else {
                val ps1Env = "PS1=$ "
                val langEnv = "LANG=en_US.UTF-8"
                val pathEnv = "PATH=" + buildPathEnv()
                val ldEnv = "LD_LIBRARY_PATH=" + buildLdLibraryEnv()
                val pwdEnv = "PWD=" + cwd
                val tmpdirEnv = "TMPDIR=${NeoTermPath.USR_PATH}/tmp"

                return arrayOf(termEnv, homeEnv, ps1Env, ldEnv, langEnv, pathEnv, pwdEnv, androidRootEnv, androidDataEnv, externalStorageEnv, tmpdirEnv)
            }
        }

        private fun buildLdLibraryEnv(): String {
            val builder = StringBuilder("${NeoTermPath.USR_PATH}/lib")

            val programSelection = NeoPreference.loadString(R.string.key_general_program_selection, NeoPreference.VALUE_NEOTERM_ONLY)
            val systemPath = System.getenv("LD_LIBRARY_PATH")

            if (programSelection != NeoPreference.VALUE_NEOTERM_ONLY) {
                builder.append(":$systemPath")
            }

            return builder.toString()
        }

        private fun buildPathEnv(): String {
            val builder = StringBuilder()
            val programSelection = NeoPreference.loadString(R.string.key_general_program_selection, NeoPreference.VALUE_NEOTERM_ONLY)
            val basePath = "${NeoTermPath.USR_PATH}/bin:${NeoTermPath.USR_PATH}/bin/applets"
            val systemPath = System.getenv("PATH")

            when (programSelection) {
                NeoPreference.VALUE_NEOTERM_ONLY -> {
                    builder.append(basePath)
                }
                NeoPreference.VALUE_NEOTERM_FIRST -> {
                    builder.append("$basePath:$systemPath")
                }
                NeoPreference.VALUE_SYSTEM_FIRST -> {
                    builder.append("$systemPath:$basePath")
                }
            }
            return builder.toString()
        }
    }

    private constructor(shellPath: String, cwd: String,
                        args: Array<String>, env: Array<String>,
                        changeCallback: SessionChangedCallback)
            : super(shellPath, cwd, args, env, changeCallback)
}