package io.neoterm.ui.customize.adapter.holder

import android.view.View
import android.widget.TextView
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import io.neoterm.R
import io.neoterm.backend.TerminalColors
import io.neoterm.ui.customize.adapter.ColorItemAdapter
import io.neoterm.ui.customize.model.ColorItem

class ColorItemViewHolder(private val rootView: View, private val listener: ColorItemAdapter.Listener) : SortedListAdapter.ViewHolder<ColorItem>(rootView) {
    private val colorItemName: TextView = rootView.findViewById<TextView>(R.id.color_item_name)
    private val colorItemDesc: TextView = rootView.findViewById<TextView>(R.id.color_item_description)
    private val colorView: View = rootView.findViewById<View>(R.id.color_item_view)

    override fun performBind(item: ColorItem) {
        rootView.setOnClickListener { listener.onModelClicked(item) }
        colorItemName.text = item.colorName
        colorItemDesc.text = item.colorValue
        if (item.colorValue.isNotEmpty()) {
            val color = TerminalColors.parse(item.colorValue)
            colorView.setBackgroundColor(color)
            colorItemDesc.setTextColor(color)
        }
    }
}
