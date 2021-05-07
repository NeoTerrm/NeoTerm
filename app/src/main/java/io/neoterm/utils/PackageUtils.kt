package io.neoterm.utils

import android.content.Context
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.floating.TerminalDialog

/**
 * @author kiva
 */
object PackageUtils {
    fun apt(context: Context, subCommand: String, extraArgs: Array<String>?, callback: (Int, TerminalDialog) -> Unit) {
        val argArray =
            if (extraArgs != null) arrayOf(NeoTermPath.APT_BIN_PATH, subCommand, *extraArgs)
            else arrayOf(NeoTermPath.APT_BIN_PATH, subCommand)

        TerminalDialog(context)
            .onFinish(object : TerminalDialog.SessionFinishedCallback {
                override fun onSessionFinished(dialog: TerminalDialog, finishedSession: TerminalSession?) {
                    val exit = finishedSession?.exitStatus ?: 1
                    callback(exit, dialog)
                }
            })
            .imeEnabled(true)
            .execute(NeoTermPath.APT_BIN_PATH, argArray)
            .show("apt $subCommand")
    }
}