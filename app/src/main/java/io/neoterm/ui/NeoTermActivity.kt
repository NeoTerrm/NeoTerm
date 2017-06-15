package io.neoterm.ui

import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v4.view.OnApplyWindowInsetsListener
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import de.mrapp.android.tabswitcher.*
import de.mrapp.android.tabswitcher.view.TabSwitcherButton
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.shortcut.ShortcutConfigLoader
import io.neoterm.customize.shortcut.builtin.BuiltinShortcutKeys
import io.neoterm.installer.BaseFileInstaller
import io.neoterm.preference.NeoPermission
import io.neoterm.preference.NeoTermPreference
import io.neoterm.services.NeoTermService
import io.neoterm.ui.settings.SettingActivity
import io.neoterm.view.tab.*


class NeoTermActivity : AppCompatActivity(), ServiceConnection {
    lateinit var tabSwitcher: TabSwitcher
    var systemShell = true
    var termService: NeoTermService? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        if (termService != null) {
            finish()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        termService = (service as NeoTermService.NeoTermBinder).service
        if (termService == null) {
            finish()
            return
        }

        var resultListener: BaseFileInstaller.ResultListener? = null
        resultListener = BaseFileInstaller.ResultListener { error ->
            if (error == null) {
                initShortcutKeys()
                systemShell = false
                if (!termService!!.sessions.isEmpty()) {
                    for (session in termService!!.sessions) {
                        addNewSession(session)
                    }
                    switchToSession(getStoredCurrentSessionOrLast())
                } else {
                    tabSwitcher.showSwitcher()
                    addNewSession("NeoTerm #0", systemShell, createRevealAnimation())
                }
            } else {
                AlertDialog.Builder(this@NeoTermActivity)
                        .setTitle("Error")
                        .setMessage(error.toString())
                        .setNegativeButton("System Shell", { _, _ ->
                            tabSwitcher.showSwitcher()
                            addNewSession("NeoTerm #0", systemShell, createRevealAnimation())
                        })
                        .setPositiveButton("Retry", { dialog, _ ->
                            dialog.dismiss()
                            BaseFileInstaller.installBaseFiles(this@NeoTermActivity, resultListener)
                        }).show()
            }
        }

        BaseFileInstaller.installBaseFiles(this, resultListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NeoPermission.initAppPermission(this, NeoPermission.REQUEST_APP_PERMISSION)
        NeoTermPreference.init(this)

        if (NeoTermPreference.loadBoolean(R.string.key_ui_fullscreen, false)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setContentView(R.layout.tab_main)

        tabSwitcher = findViewById(R.id.tab_switcher) as TabSwitcher
        tabSwitcher.decorator = TermTabDecorator(this)
        ViewCompat.setOnApplyWindowInsetsListener(tabSwitcher, createWindowInsetsListener())
        tabSwitcher.showToolbars(true)
        tabSwitcher
                .setToolbarNavigationIcon(R.drawable.ic_add_box_white_24dp, createAddSessionListener())
        tabSwitcher.inflateToolbarMenu(R.menu.tab_switcher, createToolbarMenuListener())

        val serviceIntent = Intent(this, NeoTermService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, this, 0)
    }

    private fun initShortcutKeys() {
        Thread {
            BuiltinShortcutKeys.registerAll()
            ShortcutConfigLoader.loadDefinedConfigs()
        }.start()
    }

    override fun onResume() {
        super.onResume()
        tabSwitcher.addListener(object : TabSwitcherListener {
            private var tabSwitcherButtonInit = false

            override fun onSwitcherShown(tabSwitcher: TabSwitcher) {
                if (tabSwitcherButtonInit) {
                    return
                }

                val menu = tabSwitcher.toolbarMenu
                if (menu != null) {
                    tabSwitcherButtonInit = true
                    val tabSwitcherButton = menu.findItem(R.id.toggle_tab_switcher_menu_item).actionView as TabSwitcherButton
                    tabSwitcherButton.setOnClickListener {
                        if (tabSwitcher.isSwitcherShown) {
                            tabSwitcher.hideSwitcher()
                        } else {
                            tabSwitcher.showSwitcher()
                        }
                    }
                }
            }

            override fun onSwitcherHidden(tabSwitcher: TabSwitcher) {
            }

            override fun onSelectionChanged(tabSwitcher: TabSwitcher, selectedTabIndex: Int, selectedTab: Tab?) {
                if (selectedTab is TermTab && selectedTab.termSession != null) {
                    NeoTermPreference.storeCurrentSession(selectedTab.termSession!!)
                }
            }

            override fun onTabAdded(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
                updateTabSwitcherButton()
            }

            override fun onTabRemoved(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
                if (tab is TermTab) {
                    tab.termSession?.finishIfRunning()
                    removeFinishedSession(tab.termSession)
                    tab.cleanup()
                }
                updateTabSwitcherButton()
            }

            override fun onAllTabsRemoved(tabSwitcher: TabSwitcher, tabs: Array<out Tab>, animation: Animation) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (termService != null) {
            if (termService!!.sessions.isEmpty()) {
                termService!!.stopSelf()
            }
            termService = null
        }
        unbindService(this)
        NeoTermPreference.cleanup()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (event?.action == KeyEvent.ACTION_DOWN && tabSwitcher.isSwitcherShown && tabSwitcher.count > 0) {
                    tabSwitcher.hideSwitcher()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            NeoPermission.REQUEST_APP_PERMISSION -> {
                if (grantResults.isEmpty()
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder(this).setMessage("应用无法取得必须的权限，正在退出")
                            .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                                finish()
                            })
                            .show()
                }
                return
            }
        }
    }

    private fun addNewSession(session: TerminalSession?) {
        if (session == null) {
            return
        }

        val tab = createTab(session.mSessionName) as TermTab
        tab.sessionCallback = session.sessionChangedCallback as TermSessionChangedCallback
        tab.viewClient = TermViewClient(this)
        tab.termSession = session

        addNewTab(tab, createRevealAnimation())
        switchToSession(tab)
    }

    private fun addNewSession(sessionName: String?, systemShell: Boolean, animation: Animation) {
        val tab = createTab(sessionName) as TermTab
        tab.sessionCallback = TermSessionChangedCallback()
        tab.viewClient = TermViewClient(this)
        tab.termSession = termService!!.createTermSession(null, null, null, null, tab.sessionCallback, systemShell)

        if (sessionName != null) {
            tab.termSession!!.mSessionName = sessionName
        }

        addNewTab(tab, animation)
        switchToSession(tab)
    }

    private fun switchToSession(session: TerminalSession?) {
        if (session == null) {
            return
        }

        for (i in 0..tabSwitcher.count - 1) {
            val tab = tabSwitcher.getTab(i)
            if (tab is TermTab && tab.termSession == session) {
                switchToSession(tab)
                break
            }
        }
    }

    private fun switchToSession(tab: Tab?) {
        if (tab == null) {
            return
        }
        tabSwitcher.selectTab(tab)
    }

    private fun addNewTab(tab: Tab, animation: Animation) {
        tabSwitcher.addTab(tab, 0, animation)
    }

    private fun removeFinishedSession(finishedSession: TerminalSession?) {
        if (termService == null || finishedSession == null) {
            return
        }

        termService!!.removeTermSession(finishedSession)
    }

    private fun updateTabSwitcherButton() {
        val menu = tabSwitcher.toolbarMenu
        if (menu != null) {
            val switcherButton = menu.findItem(R.id.toggle_tab_switcher_menu_item).actionView as TabSwitcherButton
            switcherButton.setCount(tabSwitcher.count)
        }
    }

    private fun getStoredCurrentSessionOrLast(): TerminalSession? {
        val stored = NeoTermPreference.getCurrentSession(termService)
        if (stored != null) return stored
        val numberOfSessions = termService!!.sessions.size
        if (numberOfSessions == 0) return null
        return termService!!.sessions[numberOfSessions - 1]
    }

    fun createAddSessionListener(): View.OnClickListener {
        return View.OnClickListener {
            val index = tabSwitcher.count
            addNewSession("NeoTerm #" + index, systemShell, createRevealAnimation())
        }
    }

    fun createToolbarMenuListener(): Toolbar.OnMenuItemClickListener {
        return Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_settings -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                R.id.menu_item_toggle_ime -> {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                    true
                }
                R.id.menu_item_new_session -> {
                    if (!tabSwitcher.isSwitcherShown) {
                        tabSwitcher.showSwitcher()
                    }
                    val index = tabSwitcher.count
                    addNewSession("NeoTerm #" + index, systemShell, createRevealAnimation())
                    true
                }
                else -> false
            }
        }
    }

    private fun createTab(tabTitle: String?): Tab {
        val tab = TermTab(tabTitle ?: "NeoTerm")
        tab.closeTabProvider = object : CloseTabProvider {
            override fun closeTab(tab: Tab) {
                tabSwitcher.showSwitcher()
                tabSwitcher.removeTab(tab)

                if (tabSwitcher.count > 1) {
                    var index = tabSwitcher.indexOf(tab)
                    if (NeoTermPreference.loadBoolean(R.string.key_ui_next_tab_anim, false)) {
                        // 关闭当前窗口后，向下一个窗口切换
                        if (--index < 0) index = tabSwitcher.count - 1
                    } else {
                        // 关闭当前窗口后，向上一个窗口切换
                        if (++index >= tabSwitcher.count) index = 0
                    }
                    switchToSession(tabSwitcher.getTab(index))
                }
            }
        }
        tab.isCloseable = true
        tab.parameters = Bundle()
        tab.setBackgroundColor(ContextCompat.getColor(this, R.color.tab_background_color))
        tab.setTitleTextColor(ContextCompat.getColor(this, R.color.tab_title_text_color))
        return tab
    }

    private fun createRevealAnimation(): Animation {
        var x = 0f
        var y = 0f
        val view = getNavigationMenuItem()

        if (view != null) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            x = location[0] + view.width / 2f
            y = location[1] + view.height / 2f
        }

        return RevealAnimation.Builder().setX(x).setY(y).create()
    }

//    private fun createPeekAnimation(): Animation {
//        return PeekAnimation.Builder().setX(tabSwitcher.width / 2f).create()
//    }

    private fun getNavigationMenuItem(): View? {
        val toolbars = tabSwitcher.toolbars

        if (toolbars != null) {
            val toolbar = if (toolbars.size > 1) toolbars[1] else toolbars[0]
            val size = toolbar.childCount

            (0..size - 1)
                    .map { toolbar.getChildAt(it) }
                    .filterIsInstance<ImageButton>()
                    .forEach { return it }
        }

        return null
    }

    private fun createWindowInsetsListener(): OnApplyWindowInsetsListener {
        return OnApplyWindowInsetsListener { _, insets ->
            tabSwitcher.setPadding(insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop, insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            insets
        }
    }
}
