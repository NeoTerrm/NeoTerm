package io.neoterm.ui.pm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.neoterm.R
import io.neoterm.component.pm.NeoPackageInfo
import io.neoterm.utils.formatSizeInKB

class PackageAdapter(
  context: Context,
  comparator: Comparator<PackageModel>,
  private val listener: PackageAdapter.Listener
) : SortedListAdapter<PackageModel>(context, PackageModel::class.java, comparator),
  FastScrollRecyclerView.SectionedAdapter {

  override fun getSectionName(position: Int): String {
    return getItem(position).packageInfo.packageName?.substring(0, 1) ?: "#"
  }

  interface Listener {
    fun onModelClicked(model: PackageModel)
  }

  override fun onCreateViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder<out PackageModel> {
    val rootView = inflater.inflate(R.layout.item_package, parent, false)
    return PackageViewHolder(rootView, listener)
  }
}

class PackageViewHolder(private val rootView: View, private val listener: PackageAdapter.Listener) :
  SortedListAdapter.ViewHolder<PackageModel>(rootView) {
  private val packageNameView: TextView = rootView.findViewById(R.id.package_item_name)
  private val packageDescView: TextView = rootView.findViewById(R.id.package_item_desc)

  override fun performBind(item: PackageModel) {
    rootView.setOnClickListener { listener.onModelClicked(item) }
    packageNameView.text = item.packageInfo.packageName
    packageDescView.text = item.packageInfo.description
  }
}

/**
 * @author kiva
 */

class PackageModel(val packageInfo: NeoPackageInfo) : SortedListAdapter.ViewModel {
  override fun <T> isSameModelAs(t: T): Boolean {
    if (t is PackageModel) {
      return t.packageInfo.packageName == packageInfo.packageName
    }
    return false
  }

  override fun <T> isContentTheSameAs(t: T): Boolean {
    return isSameModelAs(t)
  }

  fun getPackageDetails(context: Context): String {
    return context.getString(
      R.string.package_details,
      packageInfo.packageName, packageInfo.version,
      packageInfo.dependenciesString,
      packageInfo.installedSizeInBytes.formatSizeInKB(),
      packageInfo.description, packageInfo.homePage
    )
  }
}
