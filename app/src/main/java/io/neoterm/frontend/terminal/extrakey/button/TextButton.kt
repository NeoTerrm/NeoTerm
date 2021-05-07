package io.neoterm.frontend.terminal.extrakey.button

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import io.neoterm.frontend.terminal.extrakey.combine.CombinedSequence

/**
 * @author kiva
 */

open class TextButton constructor(text: String, val withEnter: Boolean = false) : IExtraButton() {
  init {
    this.buttonKeys = CombinedSequence.solveString(text)
    this.displayText = text
  }

  override fun onClick(view: View) {
    buttonKeys!!.keys.forEach {
      sendKey(view, it)
    }
    if (withEnter) {
      sendKey(view, "\n")
    }
  }

  override fun makeButton(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): Button {
    return Button(context, attrs, defStyleAttr)
  }
}
