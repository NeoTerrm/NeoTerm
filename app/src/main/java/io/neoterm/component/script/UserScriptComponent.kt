package io.neoterm.component.script

import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.preference.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
class UserScriptComponent : NeoComponent {
    override fun onServiceInit() {
        userScripts = mutableListOf()
        reloadScripts()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        reloadScripts()
    }

    lateinit var userScripts: MutableList<UserScript>
        private set

    fun reloadScripts() {
        val userScriptDir = File(NeoTermPath.USER_SCRIPT_PATH)
        userScriptDir.mkdirs()

        userScripts.clear()
        userScriptDir.listFiles()
                .takeWhile { it.canExecute() }
                .mapTo(userScripts, { UserScript(it) })
    }
}