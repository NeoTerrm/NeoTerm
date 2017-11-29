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
import io.neoterm.frontend.session.shell.client.TermCompleteListener
import io.neoterm.frontend.completion.listener.OnAutoCompleteListener
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.eks.ExtraKeysView
import io.neoterm.ui.term.NeoTermActivity
import io.neoterm.utils.TerminalUtils

/**
 * @author kiva
 */
class NeoTabDecorator(val context: NeoTermActivity) : TabSwitcherDecorator() {
    companion object {
        private val VIEW_TYPE_TERM = 1
        private val VIEW_TYPE_X = 2
    }

    override fun onInflateView(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): View {
        return when (viewType) {
            VIEW_TYPE_TERM -> {
                inflater.inflate(R.layout.ui_term, parent, false)
            }

            VIEW_TYPE_X -> {
                inflater.inflate(R.layout.ui_xorg, parent, false)
            }

            else -> {
                throw RuntimeException("Unknown view type")
            }
        }
    }

    override fun onShowTab(context: Context, tabSwitcher: TabSwitcher,
                           view: View, tab: Tab, index: Int, viewType: Int, savedInstanceState: Bundle?) {
        val toolbar = this@NeoTabDecorator.context.toolbar
        toolbar.title = if (tabSwitcher.isSwitcherShown) null else tab.title

        when (viewType) {
            VIEW_TYPE_TERM -> {
                val termTab = tab as TermTab
                termTab.toolbar = toolbar
                val terminalView = findViewById<TerminalView>(R.id.terminal_view)
                val extraKeysView = findViewById<ExtraKeysView>(R.id.extra_keys)
                bindTerminalView(termTab, terminalView, extraKeysView)
                terminalView.requestFocus()
            }

            VIEW_TYPE_X -> {
                val xtab = tab as XSessionTab
                bindXSessionView(tab)
            }
        }
    }

    private fun bindXSessionView(tab: XSessionTab) {
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
        return 2
    }

    override fun getViewType(tab: Tab, index: Int): Int {
        if (tab is TermTab) {
            return VIEW_TYPE_TERM
        } else if (tab is XSessionTab) {
            return VIEW_TYPE_X
        }
        return 0
    }
}