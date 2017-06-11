package io.neoterm.tab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.NeoTermActivity
import io.neoterm.R
import io.neoterm.terminal.TerminalSession
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
        toolbar?.title = tab.title

        val terminalView = findViewById<TerminalView>(R.id.terminal_view)
        val extraKeysView = findViewById<ExtraKeysView>(R.id.extra_keys)
        setupTerminalView(tab, terminalView, extraKeysView)
        terminalView.requestFocus()
    }

    private fun setupTerminalView(tab: Tab, view: TerminalView?, extraKeysView: ExtraKeysView?) {
        if (view == null) {
            return
        }
        view.setBackgroundColor(Color.BLACK)
        view.textSize = 30
        view.setTypeface(Typeface.MONOSPACE)

        val termTab = tab as TermTab

        // 复用前一次的 TermSession
        termTab.sessionCallback?.termView = view

        // 复用上一次的 TermViewClient
        termTab.viewClient?.termView = view
        termTab.viewClient?.extraKeysView = extraKeysView

        view.setOnKeyListener(termTab.viewClient)
        view.attachSession(termTab.termSession)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getViewType(tab: Tab, index: Int): Int {
        return 0
    }
}