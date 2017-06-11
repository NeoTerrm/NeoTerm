package io.neoterm.tab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.InputMethodManager
import de.mrapp.android.tabswitcher.Tab
import de.mrapp.android.tabswitcher.TabSwitcher
import de.mrapp.android.tabswitcher.TabSwitcherDecorator
import io.neoterm.MainActivity
import io.neoterm.R
import io.neoterm.terminal.TerminalSession
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import io.neoterm.view.TerminalViewClient

/**
 * @author kiva
 */
class TermTabDecorator(val context: MainActivity) : TabSwitcherDecorator() {
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

        setupTerminalView(terminalView, extraKeysView)
    }

    private fun setupTerminalView(view: TerminalView?, extraKeysView: ExtraKeysView?) {
        if (view == null) {
            return
        }
        view.setBackgroundColor(Color.BLACK)
        view.textSize = 30
        view.setTypeface(Typeface.MONOSPACE)

        val session = TerminalSession("/system/bin/sh", "/",
                arrayOf("/system/bin/sh"),
                arrayOf("TERM=screen", "HOME=" + context.filesDir),
                object : TerminalSession.SessionChangedCallback {
                    override fun onBell(session: TerminalSession?) {
                    }

                    override fun onClipboardText(session: TerminalSession?, text: String?) {
                    }

                    override fun onColorsChanged(session: TerminalSession?) {
                    }

                    override fun onSessionFinished(finishedSession: TerminalSession?) {
                    }

                    override fun onTextChanged(changedSession: TerminalSession?) {
                        view.onScreenUpdated()
                    }

                    override fun onTitleChanged(changedSession: TerminalSession?) {
                    }
                })

        view.setOnKeyListener(object : TerminalViewClient {
            internal var mVirtualControlKeyDown: Boolean = false
            internal var mVirtualFnKeyDown: Boolean = false

            override fun onScale(scale: Float): Float {
                if (scale < 0.9f || scale > 1.1f) {
                    val increase = scale > 1f
                    val changedSize = (if (increase) 1 else -1) * 2
                    view.textSize = view.textSize + changedSize
                    return 1.0f
                }
                return scale
            }

            override fun onSingleTapUp(e: MotionEvent?) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }

            override fun shouldBackButtonBeMappedToEscape(): Boolean {
                return false
            }

            override fun copyModeChanged(copyMode: Boolean) {
                // TODO
            }

            override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
                // TODO
                return false
            }

            override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
                return handleVirtualKeys(keyCode, e, false)
            }

            override fun readControlKey(): Boolean {
                return (extraKeysView != null && extraKeysView.readControlButton()) || mVirtualControlKeyDown
            }

            override fun readAltKey(): Boolean {
                return (extraKeysView != null && extraKeysView.readAltButton()) || mVirtualFnKeyDown
            }

            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean {
                // TODO
                return false
            }

            override fun onLongPress(event: MotionEvent?): Boolean {
                // TODO
                return false
            }

            private fun handleVirtualKeys(keyCode: Int, event: KeyEvent?, down: Boolean): Boolean {
                if (event == null) {
                    return false
                }
                val inputDevice = event.device
                if (inputDevice != null && inputDevice.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) {
                    return false
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    mVirtualControlKeyDown = down
                    return true
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    mVirtualFnKeyDown = down
                    return true
                }
                return false
            }

        })
        view.attachSession(session)
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getViewType(tab: Tab, index: Int): Int {
        return tab.parameters?.getInt("type")!!
    }

    companion object {
        val TYPE_LOADED = 1
        val TYPE_NEW = 0
    }
}