package io.neoterm.ui.term

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import io.neoterm.R
import io.neoterm.customize.script.UserScript
import io.neoterm.customize.script.UserScriptManager
import io.neoterm.frontend.ShellParameter
import io.neoterm.preference.NeoPreference
import io.neoterm.services.NeoTermService
import io.neoterm.frontend.client.TermSessionCallback
import io.neoterm.utils.TerminalUtils
import java.io.File


/**
 * @author kiva
 */
class NeoTermRemoteInterface : AppCompatActivity(), ServiceConnection {
    var termService: NeoTermService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, NeoTermService::class.java)
        startService(serviceIntent)
        if (!bindService(serviceIntent, this, 0)) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        termService = null
        unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        if (termService != null) {
            finish()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        termService = (service as NeoTermService.NeoTermBinder).service
        if (termService == null) {
            finish()
            return
        }

        handleIntent()
    }

    private fun handleIntent() {
        val className = intent.component.className.substringAfterLast('.')
        when (className) {
            "TermHere" -> handleTermHere()
            "UserScript" -> handleUserScript()
            else -> openTerm(null)
        }
    }

    private fun openTerm(initialCommand: String?) {
        // TODO: check whether system executablePath we should use
        val parameter = ShellParameter()
                .initialCommand(initialCommand)
                .callback(TermSessionCallback())
                .systemShell(false)
        val session = termService!!.createTermSession(parameter)

        // Set current session to our new one
        // In order to switch to it when entering NeoTermActivity
        NeoPreference.storeCurrentSession(session)

        val intent = Intent(this, NeoTermActivity::class.java)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun handleTermHere() {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            val extra = intent.extras.get(Intent.EXTRA_STREAM)
            if (extra is Uri) {
                val path = extra.path
                val file = File(path)
                val dirPath = if (file.isDirectory) path else file.parent
                openTerm("cd " + TerminalUtils.escapeString(dirPath))
            }
        }
        finish()
    }

    private fun handleUserScript() {
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            val extra = intent.extras.get(Intent.EXTRA_STREAM)
            val filesToHandle = mutableListOf<String>()

            when (extra) {
                is ArrayList<*> -> {
                    (0..extra.size - 1)
                            .map { extra[it] }
                            .takeWhile { it is Uri }
                            .mapTo(filesToHandle, { File((it as Uri).path).absolutePath })
                }
                is Uri -> {
                    filesToHandle.add(File(extra.path).absolutePath)
                }
            }

            UserScriptManager.reloadScripts()
            val userScripts = UserScriptManager.userScripts
            if (userScripts.isNotEmpty() && filesToHandle.isNotEmpty()) {
                setupUserScriptView(filesToHandle, userScripts)

            } else {
                Toast.makeText(this, R.string.no_user_script_found_or_files_selected, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun setupUserScriptView(filesToHandle: MutableList<String>, userScripts: List<UserScript>) {
        setContentView(R.layout.ui_user_script_list)
        val filesList = findViewById(R.id.user_script_file_list) as ListView
        val filesAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filesToHandle)
        filesList.adapter = filesAdapter
        filesList.setOnItemClickListener { _, _, position, _ ->
            AlertDialog.Builder(this@NeoTermRemoteInterface)
                    .setMessage(R.string.confirm_remove_file_from_list)
                    .setPositiveButton(android.R.string.yes, { _, _ ->
                        filesToHandle.removeAt(position)
                        filesAdapter.notifyDataSetChanged()
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show()
        }

        val scriptsList = findViewById(R.id.user_script_script_list) as ListView
        val scriptsListItem = mutableListOf<String>()
        userScripts.mapTo(scriptsListItem, { it.scriptFile.nameWithoutExtension })

        val scriptsAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, scriptsListItem)
        scriptsList.adapter = scriptsAdapter
        scriptsList.setOnItemClickListener { _, _, position, _ ->
            val script = userScripts[position].scriptFile.absoluteFile
            val argument = buildUserScriptArgument(filesToHandle)

            openTerm("$script $argument")
            finish()
        }
    }

    private fun buildUserScriptArgument(files: List<String>): String {
        val builder = StringBuilder()
        files.forEach {
            builder.append(TerminalUtils.escapeString(it))
            builder.append(" ")
        }
        return builder.toString()
    }
}