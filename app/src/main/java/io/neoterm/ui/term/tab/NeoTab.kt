package io.neoterm.ui.term.tab

import android.util.Log
import de.mrapp.android.tabswitcher.Tab

/**
 * @author kiva
 */
open class NeoTab(title: CharSequence) : Tab(title) {
    fun onPause() {}

    fun onResume() {}

    fun onStart() {}

    fun onStop() {}

    fun onWindowFocusChanged(hasFocus: Boolean) {}

    fun onDestroy() {}
}