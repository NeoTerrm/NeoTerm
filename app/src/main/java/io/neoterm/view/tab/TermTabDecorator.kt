package io.neoterm.view.tab

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.R
import io.neoterm.preference.NeoPreference
import io.neoterm.ui.NeoTermActivity
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class TermTabDecorator(val context: NeoTermActivity) : TabSwitcherDecorator() {
    override fun onInflateView(inflater: LayoutInflater, parent: ViewGroup?, viewType: Int): View {
        val view = inflater.inflate(R.layout.term, parent, false)
        val toolbar = view.findViewById(R.id.terminal_toolbar) as Toolbar
        val extraKeysView = view.findViewById(R.id.extra_keys) as ExtraKeysView

        extraKeysView.addBuiltinButton(context.fullScreenToggleButton)
        extraKeysView.updateButtons()

        toolbar.inflateMenu(R.menu.tab_switcher)
        toolbar.setOnMenuItemClickListener(context.createToolbarMenuListener())
        val menu = toolbar.menu
        TabSwitcher.setupWithMenu(context.tabSwitcher, menu, {
            toolbar.visibility = View.GONE
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
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
        view.textSize = NeoPreference.loadInt(NeoPreference.KEY_FONT_SIZE, 30)
        view.setTypeface(Typeface.MONOSPACE)
        context.fullScreenToggleButton.setStatus(NeoPreference.loadBoolean(R.string.key_ui_fullscreen, false))

        if (tab is TermTab) {
            val termTab = tab

            // 复用前一次的 TermSession
            termTab.sessionCallback?.termView = view
            termTab.sessionCallback?.termTab = termTab

            // 复用上一次的 TermViewClient
            termTab.viewClient?.termTab = termTab
            termTab.viewClient?.termView = view
            termTab.viewClient?.extraKeysView = extraKeysView

            if (termTab.termSession != null) {
                termTab.viewClient?.updateSuggestions(termTab.termSession?.title, true)
            }

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