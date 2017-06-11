package io.neoterm.tab

import android.os.Parcel
import android.os.Parcelable
import de.mrapp.android.tabswitcher.Tab
import io.neoterm.terminal.TerminalSession

/**
 * @author kiva
 */

class TermTab : Tab {
    var termSession: TerminalSession? = null
    var sessionCallback: TermSessionChangedCallback? = null
    var viewClient: TermViewClient? = null

    constructor(title: CharSequence) : super(title)

    private constructor(source: Parcel) : super(source)

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
}
