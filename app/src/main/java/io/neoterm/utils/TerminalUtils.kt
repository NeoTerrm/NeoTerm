package io.neoterm.utils

import android.app.Activity
import android.content.Context
import io.neoterm.backend.TerminalSession
import io.neoterm.component.font.FontComponent
import io.neoterm.component.session.SessionComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.NeoPreference
import io.neoterm.frontend.session.shell.ShellParameter
import io.neoterm.frontend.session.xorg.XParameter
import io.neoterm.frontend.session.xorg.XSession
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.TerminalViewClient
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView

/**
 * @author kiva
 */
object TerminalUtils {
    fun setupTerminalView(terminalView: TerminalView?, terminalViewClient: TerminalViewClient? = null) {
        terminalView?.textSize = NeoPreference.getFontSize();

        val fontComponent = ComponentManager.getComponent<FontComponent>()
        fontComponent.applyFont(terminalView, null, fontComponent.getCurrentFont())

        if (terminalViewClient != null) {
            terminalView?.setTerminalViewClient(terminalViewClient)
        }
    }

    fun setupExtraKeysView(extraKeysView: ExtraKeysView?) {
        val fontComponent = ComponentManager.getComponent<FontComponent>()
        val font = fontComponent.getCurrentFont()
        fontComponent.applyFont(null, extraKeysView, font)
    }

    fun createSession(context: Context, parameter: ShellParameter): TerminalSession {
        val sessionComponent = ComponentManager.getComponent<SessionComponent>()
        return sessionComponent.createSession(context, parameter)
    }

    fun createSession(activity: Activity, parameter: XParameter) : XSession {
        val sessionComponent = ComponentManager.getComponent<SessionComponent>()
        return sessionComponent.createSession(activity, parameter)
    }

    fun escapeString(s: String?): String {
        if (s == null) {
            return ""
        }

        val builder = StringBuilder()
        val specialChars = "\"\\$`!"
        builder.append('"')
        val length = s.length
        for (i in 0..length - 1) {
            val c = s[i]
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\')
            }
            builder.append(c)
        }
        builder.append('"')
        return builder.toString()
    }
}