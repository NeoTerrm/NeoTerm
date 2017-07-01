package io.neoterm.ui.pm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.neoterm.R
import io.neoterm.ui.pm.adapter.viewholder.PackageViewHolder
import io.neoterm.ui.pm.model.PackageModel
import java.util.*

class PackageAdapter(context: Context, comparator: Comparator<PackageModel>, private val listener: PackageAdapter.Listener, private val sectionedAdapter: FastScrollRecyclerView.SectionedAdapter?) : SortedListAdapter<PackageModel>(context, PackageModel::class.java, comparator), FastScrollRecyclerView.SectionedAdapter {

    override fun getSectionName(position: Int): String {
        return sectionedAdapter?.getSectionName(position) ?: "#"
    }

    interface Listener {
        fun onModelClicked(model: PackageModel)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): SortedListAdapter.ViewHolder<out PackageModel> {
        val rootView = inflater.inflate(R.layout.package_item, parent, false)
        return PackageViewHolder(rootView, listener)
    }
}
