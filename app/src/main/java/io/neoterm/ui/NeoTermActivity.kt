package io.neoterm.ui

import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
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
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.font.FontManager
import io.neoterm.customize.shortcut.ShortcutConfigLoader
import io.neoterm.customize.shortcut.builtin.BuiltinShortcutKeys
import io.neoterm.installer.BaseFileInstaller
import io.neoterm.preference.NeoPermission
import io.neoterm.preference.NeoPreference
import io.neoterm.services.NeoTermService
import io.neoterm.ui.settings.SettingActivity
import io.neoterm.utils.FullScreenHelper
import io.neoterm.view.eks.StatedControlButton
import io.neoterm.view.tab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NeoTermActivity : AppCompatActivity(), ServiceConnection, SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var tabSwitcher: TabSwitcher
    lateinit var fullScreenToggleButton: StatedControlButton
    var systemShell = true
    var termService: NeoTermService? = null
    var restartRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NeoPermission.initAppPermission(this, NeoPermission.REQUEST_APP_PERMISSION)
        FontManager.init(this)
        NeoPreference.init(this)

        val fullscreen = NeoPreference.loadBoolean(R.string.key_ui_fullscreen, false)

        if (fullscreen) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setContentView(R.layout.tab_main)
        FullScreenHelper.injectActivity(this, peekRecreating())
                .setKeyBoardListener({ isShow, _ ->
                    var tab: TermTab? = null

                    if (tabSwitcher.selectedTab is TermTab) {
                        tab = tabSwitcher.selectedTab as TermTab
                    }

                    tab?.viewClient?.extraKeysView?.visibility = if (isShow) View.VISIBLE else View.GONE

                    if (NeoPreference.loadBoolean(R.string.key_ui_fullscreen, false)
                            || NeoPreference.loadBoolean(R.string.key_ui_hide_toolbar, false)) {
                        tab?.toolbar?.visibility = if (isShow) View.GONE else View.VISIBLE
                    }
                })

        fullScreenToggleButton = object : StatedControlButton("FS", fullscreen) {
            override fun onClick(view: View?) {
                super.onClick(view)
                if (tabSwitcher.selectedTab is TermTab) {
                    val tab = tabSwitcher.selectedTab as TermTab
                    tab.hideIme()
                }
                NeoPreference.store(R.string.key_ui_fullscreen, super.toggleButton.isChecked)
                this@NeoTermActivity.recreate()
            }
        }

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
        if (restartRequired) {
            restartRequired = false
            recreate()
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
        tabSwitcher.addListener(object : TabSwitcherListener {
            private var tabSwitcherButtonInit = false

            override fun onSwitcherShown(tabSwitcher: TabSwitcher) {
                if (tabSwitcherButtonInit) {
                    return
                }

                val menu = tabSwitcher.toolbarMenu
                if (menu != null && !tabSwitcherButtonInit) {
                    tabSwitcherButtonInit = true
                    TabSwitcher.setupWithMenu(tabSwitcher, menu, View.OnClickListener {
                        if (tabSwitcher.isSwitcherShown) {
                            tabSwitcher.hideSwitcher()
                        } else {
                            tabSwitcher.showSwitcher()
                        }
                    })
                }
            }

            override fun onSwitcherHidden(tabSwitcher: TabSwitcher) {
            }

            override fun onSelectionChanged(tabSwitcher: TabSwitcher, selectedTabIndex: Int, selectedTab: Tab?) {
                if (selectedTab is TermTab && selectedTab.termSession != null) {
                    NeoPreference.storeCurrentSession(selectedTab.termSession!!)
                }
            }

            override fun onTabAdded(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
            }

            override fun onTabRemoved(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
                if (tab is TermTab) {
                    tab.termSession?.finishIfRunning()
                    removeFinishedSession(tab.termSession)
                    tab.cleanup()
                }
            }

            override fun onAllTabsRemoved(tabSwitcher: TabSwitcher, tabs: Array<out Tab>, animation: Animation) {
            }
        })
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        if (termService != null) {
            if (termService!!.sessions.isEmpty()) {
                termService!!.stopSelf()
            }
            termService = null
        }
        unbindService(this)
        NeoPreference.cleanup()
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
                    AlertDialog.Builder(this).setMessage(R.string.permission_denied)
                            .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                                finish()
                            })
                            .show()
                }
                return
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.key_ui_fullscreen)) {
            restartRequired = true
        }
    }

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
                        .setTitle(R.string.error)
                        .setMessage(error.toString())
                        .setNegativeButton(R.string.use_system_shell, { _, _ ->
                            tabSwitcher.showSwitcher()
                            addNewSession("NeoTerm #0", systemShell, createRevealAnimation())
                        })
                        .setPositiveButton(R.string.retry, { dialog, _ ->
                            dialog.dismiss()
                            BaseFileInstaller.installBaseFiles(this@NeoTermActivity, resultListener)
                        }).show()
            }
        }

        if (!isRecreating()) {
            BaseFileInstaller.installBaseFiles(this, resultListener)
        } else {
            systemShell = NeoPreference.loadBoolean("system_shell", true)
        }
    }

    override fun recreate() {
        NeoPreference.store("recreate", true)
        NeoPreference.store("system_shell", systemShell)
        super.recreate()
    }

    private fun isRecreating(): Boolean {
        val result = peekRecreating()
        if (result) {
            NeoPreference.store("recreate", !result)
        }
        return result
    }

    private fun peekRecreating(): Boolean {
        val result = NeoPreference.loadBoolean("recreate", false)
        return result
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

    private fun getStoredCurrentSessionOrLast(): TerminalSession? {
        val stored = NeoPreference.getCurrentSession(termService)
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

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabCloseEvent(tabCloseEvent: TabCloseEvent) {
        val tab = tabCloseEvent.termTab
        tabSwitcher.showSwitcher()
        tabSwitcher.removeTab(tab)

        if (tabSwitcher.count > 1) {
            var index = tabSwitcher.indexOf(tab)
            if (NeoPreference.loadBoolean(R.string.key_ui_next_tab_anim, false)) {
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
