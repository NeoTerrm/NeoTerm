package io.neoterm.ui.term

import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.logging.NLog
import io.neoterm.frontend.xorg.XSession
import io.neoterm.services.NeoTermService
import io.neoterm.ui.term.tab.TermTab
import io.neoterm.ui.term.tab.XSessionTab

/**
 * @author kiva
 */
object SessionRemover {
    fun removeSession(termService: NeoTermService?, tab: TermTab) {
        tab.termData.termSession?.finishIfRunning()
        removeFinishedSession(termService, tab.termData.termSession)
        tab.cleanup()
    }

    fun removeXSession(termService: NeoTermService?, tab: XSessionTab?) {
        removeFinishedSession(termService, tab?.session)
    }

    private fun removeFinishedSession(termService: NeoTermService?, finishedSession: TerminalSession?) {
        if (termService == null || finishedSession == null) {
            return
        }

        termService.removeTermSession(finishedSession)
    }

    private fun removeFinishedSession(termService: NeoTermService?, finishedSession: XSession?) {
        if (termService == null || finishedSession == null) {
            return
        }

        termService.removeXSession(finishedSession)
    }
}
