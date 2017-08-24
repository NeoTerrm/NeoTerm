package io.neoterm.ui.term.tab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.R
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.frontend.client.TermCompleteListener
import io.neoterm.ui.term.NeoTermActivity
import io.neoterm.utils.TerminalUtils
import io.neoterm.frontend.terminal.eks.ExtraKeysView
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.terminal.TerminalView

/**
 * @author kiva
 */
class TermTabDecorator(val context: NeoTermActivity) : TabSwitcherDecorator() {
    override fun onInflateView(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): View {
        val view = inflater.inflate(R.layout.ui_term, parent, false)
        return view
    }

    override fun onShowTab(context: Context, tabSwitcher: TabSwitcher,
                           view: View, tab: Tab, index: Int, viewType: Int, savedInstanceState: Bundle?) {
        val toolbar = this@TermTabDecorator.context.toolbar
        toolbar.title = if (tabSwitcher.isSwitcherShown) null else tab.title

        if (tab is TermTab) {
            tab.toolbar = toolbar
        }

        val terminalView = findViewById<TerminalView>(R.id.terminal_view)
        val extraKeysView = findViewById<ExtraKeysView>(R.id.extra_keys)
        bindTerminalView(tab, terminalView, extraKeysView)
        terminalView.requestFocus()
    }

    private fun bindTerminalView(tab: Tab, view: TerminalView?, extraKeysView: ExtraKeysView?) {
        if (view == null) {
            return
        }

        TerminalUtils.setupTerminalView(view)
        TerminalUtils.setupExtraKeysView(extraKeysView)

        val colorSchemeManager = ComponentManager.getComponent<ColorSchemeComponent>()
        colorSchemeManager.applyColorScheme(view, extraKeysView, colorSchemeManager.getCurrentColorScheme())

        if (tab is TermTab) {
            val termData = tab.termData

            TerminalUtils.setupTerminalSession(termData.termSession)

            // 复用前一次的 TermSessionCallback 和 TermViewClient
            termData.initializeViewWith(tab, view, extraKeysView)

            if (termData.termSession != null) {
                termData.viewClient?.updateExtraKeys(termData.termSession?.title, true)
            }

            view.setTerminalViewClient(termData.viewClient)
            view.attachSession(termData.termSession)

            // Still in progress with lots of bugs to deal with.
            if (NeoPreference.loadBoolean(R.string.key_general_auto_completion, false)) {
                if (termData.onAutoCompleteListener == null) {
                    termData.onAutoCompleteListener = createAutoCompleteListener(view)
                }
                view.onAutoCompleteListener = termData.onAutoCompleteListener
            }
        }
    }

    private fun createAutoCompleteListener(view: TerminalView): OnAutoCompleteListener? {
        return TermCompleteListener(view)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getViewType(tab: Tab, index: Int): Int {
        return 0
    }
}