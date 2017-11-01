package io.neoterm.frontend.client

import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.eks.ExtraKeysView

/**
 * @author kiva
 */
class TermDataHolder {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionCallback? = null
    var viewClient: TermViewClient? = null
    var onAutoCompleteListener: OnAutoCompleteListener? = null

    var termUI: TermUiPresenter? = null
    var termView: TerminalView? = null
    var extraKeysView: ExtraKeysView? = null
    var showExtraKeysView: Boolean = true

    fun cleanup() {
        onAutoCompleteListener?.onCleanUp()
        onAutoCompleteListener = null

        sessionCallback?.termData = null
        viewClient?.termData = null

        termUI = null
        termView = null
        extraKeysView = null
        termSession = null

        showExtraKeysView = true
    }

    fun initializeSessionWith(session: TerminalSession, sessionCallback: TermSessionCallback?, viewClient: TermViewClient?) {
        this.termSession = session
        this.sessionCallback = sessionCallback
        this.viewClient = viewClient
        this.sessionCallback?.termData = this
        this.viewClient?.termData = this
        this.showExtraKeysView = true
    }

    fun initializeViewWith(termUI: TermUiPresenter?, termView: TerminalView?, eks: ExtraKeysView?) {
        this.termUI = termUI
        this.termView = termView
        this.extraKeysView = eks
        this.showExtraKeysView = true
    }
}