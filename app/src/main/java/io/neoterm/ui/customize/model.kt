package io.neoterm.ui.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.neoterm.App
import io.neoterm.R
import io.neoterm.backend.TerminalColors
import io.neoterm.component.colorscheme.NeoColorScheme

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

/**
 * @author kiva
 */
class ColorItemAdapter(
  context: Context,
  initColorScheme: NeoColorScheme,
  comparator: Comparator<ColorItem>,
  private val listener: ColorItemAdapter.Listener
) : SortedListAdapter<ColorItem>(context, ColorItem::class.java, comparator), FastScrollRecyclerView.SectionedAdapter {

  val colorList = mutableListOf<ColorItem>()

  init {
    (NeoColorScheme.COLOR_TYPE_BEGIN..NeoColorScheme.COLOR_TYPE_END)
      .forEach {
        colorList.add(ColorItem(it, initColorScheme.getColor(it) ?: ""))
      }
    edit().add(colorList).commit()
  }

  interface Listener {
    fun onModelClicked(model: ColorItem)
  }

  override fun getSectionName(position: Int): String {
    return colorList[position].colorName[0].toString()
  }

  override fun onCreateViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder<out ColorItem> {
    val rootView = inflater.inflate(R.layout.item_color, parent, false)
    return ColorItemViewHolder(rootView, listener)
  }
}

class ColorItemViewHolder(private val rootView: View, private val listener: ColorItemAdapter.Listener) :
  SortedListAdapter.ViewHolder<ColorItem>(rootView) {
  private val colorItemName: TextView = rootView.findViewById(R.id.color_item_name)
  private val colorItemDesc: TextView = rootView.findViewById(R.id.color_item_description)
  private val colorView: View = rootView.findViewById(R.id.color_item_view)

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
