package io.neoterm.component.session

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import io.neoterm.Globals
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.frontend.session.shell.ShellParameter
import io.neoterm.frontend.session.shell.ShellTermSession
import io.neoterm.frontend.session.xorg.XParameter
import io.neoterm.frontend.session.xorg.XSession
import io.neoterm.frontend.session.xorg.client.XSessionData
import io.neoterm.utils.TerminalUtils

/**
 * @author kiva
 */
class SessionComponent : NeoComponent {
    companion object {
        private var IS_LIBRARIES_LOADED = false

        private fun wrapLibraryName(libName: String): String {
            return "lib$libName.so"
        }

        @SuppressLint("UnsafeDynamicallyLoadedCode")
        private fun loadLibraries(): Boolean {
            try {
                if (Globals.NeedGles3) {
                    System.loadLibrary("GLESv3")
                    NLog.e("SessionComponent", "Loaded GLESv3 lib")
                } else if (Globals.NeedGles2) {
                    System.loadLibrary("GLESv2")
                    NLog.e("SessionComponent", "Loaded GLESv2 lib")
                }
            } catch (e: UnsatisfiedLinkError) {
                NLog.e("SessionComponent", "Cannot load GLESv3 or GLESv2 lib")
            }

            var result: Boolean
            try {
                Globals.XLIBS
                        .plus(Globals.XAPP_LIBS)
                        .forEach {
                            val soPath = "${NeoTermPath.LIB_PATH}/xorg-neoterm/${wrapLibraryName(it)}"
                            NLog.e("SessionComponent", "Loading lib " + soPath)
                            try {
                                System.load(soPath)
                            } catch (error: UnsatisfiedLinkError) {
                                NLog.e("SessionComponent", "Error loading lib " + soPath
                                        + ", reason: " + error.localizedMessage)
                                result = false
                            }
                        }
                result = true

            } catch (ignore: UnsatisfiedLinkError) {
                NLog.e("SessionComponent", ignore.localizedMessage)
                result = false
            }

            return result
        }

        private fun checkLibrariesLoaded(): Boolean {
            if (!IS_LIBRARIES_LOADED) {
                synchronized(SessionComponent::class.java) {
                    if (!IS_LIBRARIES_LOADED) {
                        IS_LIBRARIES_LOADED = loadLibraries()
                    }
                }
            }
            return IS_LIBRARIES_LOADED
        }
    }

    override fun onServiceInit() {
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
    }

    fun createSession(context: Context, parameter: XParameter): XSession {
        if (context is Activity) {
            if (!checkLibrariesLoaded()) {
                throw RuntimeException("Cannot load libraries!")
            }

            return XSession(context, XSessionData())
        }
        throw RuntimeException("Creating X sessions requires Activity, but got Context")
    }

    fun createSession(context: Context, parameter: ShellParameter): ShellTermSession {
        val session = ShellTermSession.Builder()
                .executablePath(parameter.executablePath)
                .currentWorkingDirectory(parameter.cwd)
                .callback(parameter.sessionCallback)
                .systemShell(parameter.systemShell)
                .envArray(parameter.env)
                .argArray(parameter.arguments)
                .initialCommand(parameter.initialCommand)
                .profile(parameter.shellProfile)
                .create(context)
        TerminalUtils.setupTerminalSession(session)
        return session
    }
}