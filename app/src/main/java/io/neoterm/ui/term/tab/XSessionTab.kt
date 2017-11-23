package io.neoterm.ui.term.tab

import de.mrapp.android.tabswitcher.Tab
import io.neoterm.frontend.xorg.XSession

/**
 * @author kiva
 */
class XSessionTab(title: CharSequence) : Tab(title) {
    var session: XSession? = null
}
