package io.neoterm.ui.term

import io.neoterm.ui.term.tab.TermTab

/**
 * @author kiva
 */
object SessionRemover {
    fun removeSession(tab: TermTab) {
        tab.termData.termSession?.finishIfRunning()
        tab.cleanup()
    }
}