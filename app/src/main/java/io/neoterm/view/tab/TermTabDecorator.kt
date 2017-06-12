package io.neoterm.view.tab

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.ui.NeoTermActivity
import io.neoterm.R
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class TermTabDecorator(val context: NeoTermActivity) : TabSwitcherDecorator() {
    override fun onInflateView(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): View {
        val view = inflater.inflate(R.layout.term, parent, false)
        val toolbar = view.findViewById(R.id.terminal_toolbar) as Toolbar

        toolbar.inflateMenu(R.menu.tab_switcher)
        toolbar.setOnMenuItemClickListener(context.createToolbarMenuListener())
        val menu = toolbar.menu
        TabSwitcher.setupWithMenu(context.tabSwitcher, menu, {
            context.tabSwitcher.showSwitcher()
        })

        return view
    }

    override fun onShowTab(context: Context, tabSwitcher: TabSwitcher,
                           view: View, tab: Tab, index: Int, viewType: Int, savedInstanceState: Bundle?) {
        val toolbar = findViewById<Toolbar>(R.id.terminal_toolbar)
        toolbar.title = tab.title

        if (tabSwitcher.isSwitcherShown) {
            toolbar.visibility = View.GONE
        } else {
            toolbar.visibility = View.VISIBLE
        }

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
        view.textSize = 30
        view.setTypeface(Typeface.MONOSPACE)

        if (tab is TermTab) {
            val termTab = tab

            // 复用前一次的 TermSession
            termTab.sessionCallback?.termView = view
            termTab.sessionCallback?.termTab = tab

            // 复用上一次的 TermViewClient
            termTab.viewClient?.termView = view
            termTab.viewClient?.extraKeysView = extraKeysView

            view.setOnKeyListener(termTab.viewClient)
            view.attachSession(termTab.termSession)
        }
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getViewType(tab: Tab, index: Int): Int {
        return 0
    }
}