package io.neoterm.customize.script

import android.content.Context
import io.neoterm.preference.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
object UserScriptManager {
    lateinit var userScripts: MutableList<UserScript>
        private set

    fun init(context: Context) {
        userScripts = mutableListOf()
        reloadScripts()
    }

    fun reloadScripts() {
        val userScriptDir = File(NeoTermPath.USER_SCRIPT_PATH)
        userScriptDir.mkdirs()

        userScripts.clear()
        userScriptDir.listFiles()
                .takeWhile { it.canExecute() }
                .mapTo(userScripts, { UserScript(it) })
    }
}