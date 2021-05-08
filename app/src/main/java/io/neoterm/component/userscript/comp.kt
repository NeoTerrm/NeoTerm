package io.neoterm.component.userscript

import android.content.Context
import android.system.Os
import io.neoterm.App
import io.neoterm.component.NeoComponent
import io.neoterm.component.config.NeoTermPath
import io.neoterm.utils.NLog
import io.neoterm.utils.extractAssetsDir
import java.io.File

class UserScript(val scriptFile: File)

class UserScriptComponent : NeoComponent {
  var userScripts = listOf<UserScript>()
  private val scriptDir = File(NeoTermPath.USER_SCRIPT_PATH)

  override fun onServiceInit() = checkForFiles()

  override fun onServiceDestroy() {
  }

  override fun onServiceObtained() = checkForFiles()

  private fun extractDefaultScript(context: Context) = kotlin.runCatching {
    context.extractAssetsDir("scripts", NeoTermPath.USER_SCRIPT_PATH)
    scriptDir.listFiles().forEach {
      Os.chmod(it.absolutePath, 448 /*Dec of 0700*/)
    }
  }.onFailure {
    NLog.e("UserScript", "Failed to extract default user scripts: ${it.localizedMessage}")
  }

  private fun checkForFiles() {
    extractDefaultScript(App.get())
    reloadScripts()
  }

  private fun reloadScripts() {
    userScripts = scriptDir.listFiles()
      .takeWhile { it.canExecute() }
      .map { UserScript(it) }
      .toList()
  }
}
