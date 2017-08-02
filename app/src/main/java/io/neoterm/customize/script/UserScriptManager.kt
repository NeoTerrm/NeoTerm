package io.neoterm.customize.script

import io.neoterm.frontend.service.NeoService
import io.neoterm.preference.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
class UserScriptManager : NeoService {
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