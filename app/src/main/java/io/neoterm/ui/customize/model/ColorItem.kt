package io.neoterm.ui.customize.model

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import io.neoterm.App
import io.neoterm.R
import io.neoterm.component.color.NeoColorScheme

/**
 * @author kiva
 */
class ColorItem(var colorType: Int, var colorValue: String) : SortedListAdapter.ViewModel {
    override fun <T> isSameModelAs(t: T): Boolean {
        if (t is ColorItem) {
            return t.colorName == colorName
                    && t.colorValue == colorValue
                    && t.colorType == colorType
        }
        return false
    }

    override fun <T> isContentTheSameAs(t: T): Boolean {
        return isSameModelAs(t)
    }

    var colorName = App.get().resources
            .getStringArray(R.array.color_item_names)[colorType - NeoColorScheme.COLOR_TYPE_BEGIN]
}