package io.neoterm.component.script

import android.content.Context
import android.system.Os
import io.neoterm.App
import io.neoterm.frontend.component.NeoComponent
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.utils.AssetsUtils
import java.io.File

/**
 * @author kiva
 */
class UserScriptComponent : NeoComponent {
    lateinit var userScripts: MutableList<UserScript>

    override fun onServiceInit() {
        checkForFiles()
    }

    override fun onServiceDestroy() {
    }

    override fun onServiceObtained() {
        checkForFiles()
    }

    private fun extractDefaultScript(context: Context): Boolean {
        try {
            AssetsUtils.extractAssetsDir(context, "scripts", NeoTermPath.USER_SCRIPT_PATH)
            File(NeoTermPath.USER_SCRIPT_PATH)
                    .listFiles().forEach {
                Os.chmod(it.absolutePath, 448 /*Dec of 0700*/)
            }
            return true
        } catch (e: Exception) {
            NLog.e("UserScript", "Failed to extract default user scripts: ${e.localizedMessage}")
            return false
        }
    }

    private fun checkForFiles() {
        File(NeoTermPath.USER_SCRIPT_PATH).mkdirs()
        userScripts = mutableListOf()

        extractDefaultScript(App.get())
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