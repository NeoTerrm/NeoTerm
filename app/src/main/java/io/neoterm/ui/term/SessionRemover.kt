package io.neoterm.ui.term

import io.neoterm.backend.TerminalSession
import io.neoterm.services.NeoTermService
import io.neoterm.ui.term.tab.TermTab

/**
 * @author kiva
 */
object SessionRemover {
    fun removeSession(termService: NeoTermService?, tab: TermTab) {
        tab.termData.termSession?.finishIfRunning()
        removeFinishedSession(termService, tab.termData.termSession)
        tab.cleanup()
    }

    private fun removeFinishedSession(termService: NeoTermService?, finishedSession: TerminalSession?) {
        if (termService == null || finishedSession == null) {
            return
        }

        termService.removeTermSession(finishedSession)
    }
}