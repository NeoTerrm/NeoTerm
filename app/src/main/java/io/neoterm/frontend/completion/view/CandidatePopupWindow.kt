package io.neoterm.frontend.completion.view

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import io.neoterm.R
import io.neoterm.backend.TerminalColors
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.frontend.completion.listener.OnCandidateSelectedListener
import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.terminal.TerminalView

/**
 * @author kiva
 */
class CandidatePopupWindow(val context: Context) {
    var candidates: List<CompletionCandidate>? = null
    var onCandidateSelectedListener: OnCandidateSelectedListener? = null

    private var popupWindow: PopupWindow? = null
    private var wantsToFinish = false
    private var candidateAdapter: CandidateAdapter? = null
    private var candidateListView: ListView? = null

    fun show(terminalView: TerminalView) {
        if (popupWindow == null && !wantsToFinish) {
            popupWindow = createPopupWindow()
        }

        candidateAdapter?.notifyDataSetChanged()

        val popWindow = popupWindow
        if (popWindow != null) {
            // Ensure that the popup window will not cover the IME.
            val rootView = popWindow.contentView
            if (rootView is MaxHeightView) {
                val maxHeight = terminalView.height
                rootView.setMaxHeight(maxHeight)
            }

            popWindow.showAtLocation(terminalView, Gravity.BOTTOM.and(Gravity.START),
                    terminalView.cursorAbsX,
                    terminalView.cursorAbsY)
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    fun isShowing(): Boolean {
        return popupWindow?.isShowing ?: false
    }

    private fun createPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(context)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        val contentView = LayoutInflater.from(context).inflate(R.layout.popup_auto_complete, null, false)
        val listView = contentView.findViewById<ListView>(R.id.popup_complete_candidate_list)
        candidateAdapter = CandidateAdapter(this)
        listView.adapter = candidateAdapter
        listView.setOnItemClickListener({ _, _, position, _ ->
            val selectedItem = candidates?.get(position)
            if (selectedItem != null) {
                onCandidateSelectedListener?.onCandidateSelected(selectedItem)
            }
        })

        candidateListView = listView
        popupWindow.contentView = contentView
        return popupWindow
    }

    fun cleanup() {
        wantsToFinish = true
        popupWindow = null
        candidateListView = null
        candidateAdapter = null
        candidates = null
    }

    class CandidateAdapter(val candidatePopupWindow: CandidatePopupWindow) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val viewHolder: CandidateViewHolder =
                    if (convertView != null) {
                        convertView.tag as CandidateViewHolder
                    } else {
                        convertView = LayoutInflater.from(candidatePopupWindow.context)
                                .inflate(R.layout.item_complete_candidate, null, false)
                        val viewHolder = CandidateViewHolder(convertView)
                        convertView.tag = viewHolder
                        viewHolder
                    }

            val candidate = getItem(position) as CompletionCandidate
            viewHolder.apply {
                display.text = candidate.displayName
                if (candidate.description != null) {
                    splitView.visibility = View.VISIBLE
                    description.visibility = View.VISIBLE
                    description.text = candidate.description
                } else {
                    splitView.visibility = View.GONE
                    description.visibility = View.GONE
                }
            }
            return convertView!!
        }

        override fun getItem(position: Int): Any? {
            return candidatePopupWindow.candidates?.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return candidatePopupWindow.candidates?.size ?: 0
        }
    }

    class CandidateViewHolder(rootView: View) {
        val display: TextView = rootView.findViewById<TextView>(R.id.complete_display)
        val description: TextView = rootView.findViewById<TextView>(R.id.complete_description)
        val splitView: View = rootView.findViewById(R.id.complete_split)

        init {
            val colorScheme = ComponentManager.getComponent<ColorSchemeComponent>().getCurrentColorScheme()
            val textColor = TerminalColors.parse(colorScheme.foregroundColor)
            display.setTextColor(textColor)
            description.setTextColor(textColor)
        }
    }
}