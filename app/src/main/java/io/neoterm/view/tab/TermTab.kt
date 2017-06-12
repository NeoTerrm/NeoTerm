package io.neoterm.view.tab

import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.Toolbar
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.backend.TerminalSession

/**
 * @author kiva
 */

class TermTab : Tab {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionChangedCallback? = null
    var viewClient: TermViewClient? = null
    var toolbar: Toolbar? = null

    constructor(title: CharSequence) : super(title)

    private constructor(source: Parcel) : super(source)

    fun cleanup() {
        termSession?.finishIfRunning()
        viewClient?.termView = null
        viewClient?.extraKeysView = null
        sessionCallback?.termView = null
        sessionCallback?.termTab = null
        toolbar = null
        termSession = null
    }

    companion object {
        val CREATOR: Parcelable.Creator<TermTab> = object : Parcelable.Creator<TermTab> {
            override fun createFromParcel(source: Parcel): TermTab {
                return TermTab(source)
            }

            override fun newArray(size: Int): Array<TermTab?> {
                return arrayOfNulls(size)
            }
        }
    }

    fun updateTitle(title: String) {
        this.title = title
        toolbar?.title = title
    }
}
