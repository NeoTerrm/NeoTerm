package io.neoterm.ui.term.tab

import android.content.res.Configuration
import android.util.Log
import de.mrapp.android.tabswitcher.Tab

/**
 * @author kiva
 */
open class NeoTab(title: CharSequence) : Tab(title) {
    open fun onPause() {}

    open fun onResume() {}

    open fun onStart() {}

    open fun onStop() {}

    open fun onWindowFocusChanged(hasFocus: Boolean) {}

    open fun onDestroy() {}

    open fun onConfigurationChanged(newConfig: Configuration) {}
}