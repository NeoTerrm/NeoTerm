package io.neoterm.ui.pm.model

import android.content.Context
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import io.neoterm.R

import io.neoterm.component.pm.NeoPackageInfo
import io.neoterm.utils.FileUtils

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
      FileUtils.formatSizeInKB(packageInfo.installedSizeInBytes),
      packageInfo.description, packageInfo.homePage
    )
  }
}
