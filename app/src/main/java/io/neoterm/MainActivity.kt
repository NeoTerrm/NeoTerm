package io.neoterm

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.OnApplyWindowInsetsListener
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import de.mrapp.android.tabswitcher.*
import io.neoterm.tab.TermTabDecorator


class MainActivity : AppCompatActivity() {
    lateinit var tabSwitcher: TabSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tab_main)

        tabSwitcher = findViewById(R.id.tab_switcher) as TabSwitcher
        tabSwitcher.decorator = TermTabDecorator(this)
        ViewCompat.setOnApplyWindowInsetsListener(tabSwitcher, createWindowInsetsListener())
        tabSwitcher.showToolbars(true)
        tabSwitcher
                .setToolbarNavigationIcon(R.drawable.ic_add_box_white_24dp, createAddTabListener())
        tabSwitcher.inflateToolbarMenu(R.menu.tab_switcher, createToolbarMenuListener())
        tabSwitcher.addListener(object : TabSwitcherListener {
            override fun onSwitcherShown(tabSwitcher: TabSwitcher) {
            }

            override fun onSwitcherHidden(tabSwitcher: TabSwitcher) {
            }

            override fun onSelectionChanged(tabSwitcher: TabSwitcher, selectedTabIndex: Int, selectedTab: Tab?) {
            }

            override fun onTabAdded(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
            }

            override fun onTabRemoved(tabSwitcher: TabSwitcher, index: Int, tab: Tab, animation: Animation) {
            }

            override fun onAllTabsRemoved(tabSwitcher: TabSwitcher, tabs: Array<out Tab>, animation: Animation) {
            }
        })
    }

    fun createAddTabListener(): View.OnClickListener {
        return View.OnClickListener {
            val index = tabSwitcher.count
            val animation = createRevealAnimation()
            tabSwitcher.addTab(createTab(index), 0, animation)
        }
    }

    fun createToolbarMenuListener(): Toolbar.OnMenuItemClickListener {
        return Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.add_tab_menu_item -> {
                    val index = tabSwitcher.count
                    val tab = createTab(index)

                    if (tabSwitcher.isSwitcherShown) {
                        tabSwitcher.addTab(tab, 0, createRevealAnimation())
                    } else {
                        tabSwitcher.addTab(tab, 0, createPeekAnimation())
                    }

                    true
                }
                else -> false
            }
        }
    }

    private fun createTab(index: Int): Tab {
        val tab = Tab("Neo Term #" + index)
        tab.isCloseable = true
        tab.parameters = Bundle()
        tab.parameters?.putInt("type", TermTabDecorator.TYPE_NEW)
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

    private fun createPeekAnimation(): Animation {
        return PeekAnimation.Builder().setX(tabSwitcher.width / 2f).create()
    }

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
        return OnApplyWindowInsetsListener { v, insets ->
            tabSwitcher.setPadding(insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop, insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            insets
        }
    }
}
