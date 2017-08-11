package io.neoterm.ui.pm.adapter.viewholder

import android.view.View
import android.widget.TextView

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter

import io.neoterm.R
import io.neoterm.ui.pm.adapter.PackageAdapter
import io.neoterm.ui.pm.model.PackageModel

class PackageViewHolder(private val rootView: View, private val listener: PackageAdapter.Listener) : SortedListAdapter.ViewHolder<PackageModel>(rootView) {
    private val packageNameView: TextView = rootView.findViewById<TextView>(R.id.package_item_name)
    private val packageDescView: TextView = rootView.findViewById<TextView>(R.id.package_item_desc)

    override fun performBind(item: PackageModel) {
        rootView.setOnClickListener { listener.onModelClicked(item) }
        packageNameView.text = item.packageInfo.packageName
        packageDescView.text = item.packageInfo.description
    }
}
