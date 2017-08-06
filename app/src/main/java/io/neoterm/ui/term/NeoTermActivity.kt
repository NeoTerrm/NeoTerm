package io.neoterm.ui.term

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import de.mrapp.android.tabswitcher.*
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.setup.BaseFileInstaller
import io.neoterm.frontend.shell.ShellParameter
import io.neoterm.frontend.client.TermSessionCallback
import io.neoterm.frontend.client.TermViewClient
import io.neoterm.frontend.preference.NeoPermission
import io.neoterm.frontend.preference.NeoPreference
import io.neoterm.services.NeoTermService
import io.neoterm.ui.bonus.BonusActivity
import io.neoterm.ui.pm.PackageManagerActivity
import io.neoterm.ui.settings.SettingActivity
import io.neoterm.ui.setup.SetupActivity
import io.neoterm.ui.term.tab.TermTab
import io.neoterm.ui.term.tab.TermTabDecorator
import io.neoterm.ui.term.event.TabCloseEvent
import io.neoterm.ui.term.event.TitleChangedEvent
import io.neoterm.ui.term.event.ToggleFullScreenEvent
import io.neoterm.ui.term.event.ToggleImeEvent
import io.neoterm.utils.FullScreenHelper
import io.neoterm.view.eks.button.StatedControlButton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NeoTermActivity : AppCompatActivity(), ServiceConnection, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val KEY_NO_RESTORE = "no_restore"
        const val REQUEST_SETUP = 22313
    }

    lateinit var tabSwitcher: TabSwitcher
    lateinit var fullScreenToggleButton: StatedControlButton
    lateinit var fullScreenHelper: FullScreenHelper
    lateinit var toolbar: Toolbar
    var addSessionListener = createAddSessionListener()
    var termService: NeoTermService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        NeoPreference.init(this)
        NeoPermission.initAppPermission(this, NeoPermission.REQUEST_APP_PERMISSION)

        val fullscreen = NeoPreference.loadBoolean(R.string.key_ui_fullscreen, false)
        if (fullscreen) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setContentView(R.layout.ui_main)

        toolbar = findViewById(R.id.terminal_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fullScreenHelper = FullScreenHelper.injectActivity(this, fullscreen, peekRecreating())
        fullScreenHelper.setKeyBoardListener(object : FullScreenHelper.KeyBoardListener {
            override fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int) {
                var tab: TermTab? = null

                if (tabSwitcher.selectedTab is TermTab) {
                    tab = tabSwitcher.selectedTab as TermTab
                }

                if (NeoPreference.loadBoolean(R.string.key_ui_fullscreen, false)
                        || NeoPreference.loadBoolean(R.string.key_ui_hide_toolbar, false)) {
                    tab?.toolbar?.visibility = if (isShow) View.GONE else View.VISIBLE
                }
            }
        })

        tabSwitcher = findViewById(R.id.tab_switcher) as TabSwitcher
        tabSwitcher.decorator = TermTabDecorator(this)
        ViewCompat.setOnApplyWindowInsetsListener(tabSwitcher, createWindowInsetsListener())
        tabSwitcher.showToolbars(false)

        val serviceIntent = Intent(this, NeoTermService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, this, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        TabSwitcher.setupWithMenu(tabSwitcher, toolbar.menu, View.OnClickListener {
            val imm = this@NeoTermActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!tabSwitcher.isSwitcherShown) {
                if (imm.isActive && tabSwitcher.selectedTab is TermTab) {
                    val tab = tabSwitcher.selectedTab as TermTab
                    tab.requireHideIme()
                }
                toggleSwitcher(showSwitcher = true, easterEgg = true)
            } else {
                toggleSwitcher(showSwitcher = false, easterEgg = true)
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_settings -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            R.id.menu_item_package_settings -> {
                startActivity(Intent(this, PackageManagerActivity::class.java))
                true
            }
            R.id.menu_item_float_up -> {
                val tab = tabSwitcher.selectedTab
                if (tab != null && tab is TermTab) {
                    floatTabUp(tab)
                }
                true
            }
            R.id.menu_item_new_session -> {
                if (!tabSwitcher.isSwitcherShown) {
                    toggleSwitcher(showSwitcher = true, easterEgg = false)
                }
                val index = tabSwitcher.count
                addNewSession("NeoTerm #" + index, getSystemShellMode(), createRevealAnimation())
                true
            }
            R.id.menu_item_new_system_session -> {
                forceAddSystemSession()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
        tabSwitcher.addListener(object : TabSwitcherListener {
            override fun onSwitcherShown(tabSwitcher: TabSwitcher) {
                toolbar.setNavigationIcon(R.drawable.ic_add_box_white_24dp)
                toolbar.setNavigationOnClickListener(addSessionListener)
                toolbar.setBackgroundResource(android.R.color.transparent)
            }

            override fun onSwitcherHidden(tabSwitcher: TabSwitcher) {
                toolbar.navigationIcon = null
                toolbar.setNavigationOnClickListener(null)
                toolbar.setBackgroundResource(R.color.colorPrimaryDark)
            }

            override fun onSelectionChanged(tabSwitcher: TabSwitcher, selectedTabIndex: Int, selectedTab: Tab?) {
                if (selectedTab is TermTab && selectedTab.termData.termSession != null) {
                    NeoPreference.storeCurrentSession(selectedTab.termData.termSession!!)
                }
            }

            override fun onTabAdded(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
            }

            override fun onTabRemoved(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
                if (tab is TermTab) {
                    tab.termData.termSession?.finishIfRunning()
                    removeFinishedSession(tab.termData.termSession)
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
        (0..tabSwitcher.count - 1)
                .map { tabSwitcher.getTab(it) }
                .takeWhile { it is TermTab }
                .forEach {
                    val termTab = it as TermTab
                    // After stopped, window locations may changed
                    // Rebind it at next time.
                    termTab.resetAutoCompleteStatus()
                }
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
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (event?.action == KeyEvent.ACTION_DOWN && tabSwitcher.isSwitcherShown && tabSwitcher.count > 0) {
                    toggleSwitcher(showSwitcher = false, easterEgg = false)
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
            setFullScreenMode(NeoPreference.loadBoolean(key, false))
        } else if (key == getString(R.string.key_customization_color_scheme)) {
            if (tabSwitcher.count > 0) {
                val tab = tabSwitcher.selectedTab
                if (tab is TermTab) {
                    tab.updateColorScheme()
                }
            }
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

        if (!isRecreating()) {
            if (BaseFileInstaller.needSetup()) {
                val intent = Intent(this, SetupActivity::class.java)
                intent.putExtra("setup", true)
                startActivityForResult(intent, REQUEST_SETUP)
                return
            }
            enterMain()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SETUP -> {
                when (resultCode) {
                    Activity.RESULT_OK -> enterMain()
                    Activity.RESULT_CANCELED -> {
                        setSystemShellMode(true)
                        forceAddSystemSession()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun floatTabUp(tab: TermTab) {
        Toast.makeText(this, "In Progress", Toast.LENGTH_SHORT).show()
    }

    private fun forceAddSystemSession() {
        if (!tabSwitcher.isSwitcherShown) {
            toggleSwitcher(showSwitcher = true, easterEgg = false)
        }
        val index = tabSwitcher.count

        // Fore system shell mode to be enabled.
        addNewSession("NeoTerm #" + index, true, createRevealAnimation())
    }

    private fun enterMain() {
        setSystemShellMode(false)

        if (!termService!!.sessions.isEmpty()) {
            for (session in termService!!.sessions) {
                addNewSession(session)
            }
            switchToSession(getStoredCurrentSessionOrLast())
        } else {
            toggleSwitcher(showSwitcher = true, easterEgg = false)
            // Fore system shell mode to be disabled.
            addNewSession("NeoTerm #0", false, createRevealAnimation())
        }
    }

    override fun recreate() {
        NeoPreference.store(KEY_NO_RESTORE, true)
        saveCurrentStatus()
        super.recreate()
    }

    private fun isRecreating(): Boolean {
        val result = peekRecreating()
        if (result) {
            NeoPreference.store(KEY_NO_RESTORE, !result)
        }
        return result
    }

    private fun saveCurrentStatus() {
        setSystemShellMode(getSystemShellMode())
    }

    private fun peekRecreating(): Boolean {
        val result = NeoPreference.loadBoolean(KEY_NO_RESTORE, false)
        return result
    }

    private fun setFullScreenMode(fullScreen: Boolean) {
        fullScreenHelper.fullScreen = fullScreen
        if (tabSwitcher.selectedTab is TermTab) {
            val tab = tabSwitcher.selectedTab as TermTab
            tab.requireHideIme()
            tab.onFullScreenModeChanged(fullScreen)
        }
        NeoPreference.store(R.string.key_ui_fullscreen, fullScreen)
        this@NeoTermActivity.recreate()
    }

    private fun addNewSession(session: TerminalSession?) {
        if (session == null) {
            return
        }

        // Do not add the same session again
        // Or app will crash when rotate
        val tabCount = tabSwitcher.count
        (0..(tabCount - 1))
                .map { tabSwitcher.getTab(it) }
                .filter { it is TermTab && it.termData.termSession == session }
                .forEach { return }

        val sessionCallback = session.sessionChangedCallback as TermSessionCallback
        val viewClient = TermViewClient(this)

        val tab = createTab(session.title) as TermTab
        tab.termData.initializeSessionWith(session, sessionCallback, viewClient)

        addNewTab(tab, createRevealAnimation())
        switchToSession(tab)
    }

    private fun addNewSession(sessionName: String?, systemShell: Boolean, animation: Animation) {
        val sessionCallback = TermSessionCallback()
        val viewClient = TermViewClient(this)

        val parameter = ShellParameter()
                .callback(sessionCallback)
                .systemShell(systemShell)
        val session = termService!!.createTermSession(parameter)

        if (sessionName != null) {
            session.mSessionName = sessionName
        }

        val tab = createTab(sessionName) as TermTab
        tab.termData.initializeSessionWith(session, sessionCallback, viewClient)

        addNewTab(tab, animation)
        switchToSession(tab)
    }

    private fun switchToSession(session: TerminalSession?) {
        if (session == null) {
            return
        }

        for (i in 0..tabSwitcher.count - 1) {
            val tab = tabSwitcher.getTab(i)
            if (tab is TermTab && tab.termData.termSession == session) {
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
            addNewSession("NeoTerm #" + index, getSystemShellMode(), createRevealAnimation())
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

    private fun toggleSwitcher(showSwitcher: Boolean, easterEgg: Boolean) {
        if (tabSwitcher.count > 0) {
            if (showSwitcher) {
                val transparentAnimator = ObjectAnimator.ofFloat(toolbar, View.ALPHA, 1.0f, 0.0f, 1.0f)
                transparentAnimator.interpolator = AccelerateDecelerateInterpolator()
                transparentAnimator.start()
            }
        } else if (easterEgg) {
            val happyCount = NeoPreference.loadInt(NeoPreference.KEY_HAPPY_EGG, 0) + 1
            NeoPreference.store(NeoPreference.KEY_HAPPY_EGG, happyCount)

            val trigger = NeoPreference.VALUE_HAPPY_EGG_TRIGGER

            if (happyCount == trigger / 2) {
                @SuppressLint("ShowToast")
                val toast = Toast.makeText(this, "Emm...", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            } else if (happyCount > trigger) {
                NeoPreference.store(NeoPreference.KEY_HAPPY_EGG, 0)
                startActivity(Intent(this, BonusActivity::class.java))
            }
            return
        }
        if (showSwitcher) {
            tabSwitcher.showSwitcher()
        } else {
            tabSwitcher.hideSwitcher()
        }
    }

    private fun setSystemShellMode(systemShell: Boolean) {
        NeoPreference.store(NeoPreference.KEY_SYSTEM_SHELL, systemShell)
    }

    private fun getSystemShellMode(): Boolean {
        return NeoPreference.loadBoolean(NeoPreference.KEY_SYSTEM_SHELL, true)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabCloseEvent(tabCloseEvent: TabCloseEvent) {
        val tab = tabCloseEvent.termTab
        toggleSwitcher(showSwitcher = true, easterEgg = false)
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

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleFullScreenEvent(toggleFullScreenEvent: ToggleFullScreenEvent) {
        val fullScreen = fullScreenHelper.fullScreen
        setFullScreenMode(!fullScreen)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onToggleImeEvent(toggleImeEvent: ToggleImeEvent) {
        if (!tabSwitcher.isSwitcherShown) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
    }


    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTitleChangedEvent(titleChangedEvent: TitleChangedEvent) {
        if (!tabSwitcher.isSwitcherShown) {
            toolbar.title = titleChangedEvent.title
        }
    }

}
