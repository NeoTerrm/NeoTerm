package io.neoterm.view.eks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ToggleButton

import java.io.File
import java.util.ArrayList

import io.neoterm.customize.eks.EksConfigParser
import io.neoterm.preference.NeoTermPath
import io.neoterm.utils.FileUtils
import io.neoterm.view.eks.button.ControlButton
import io.neoterm.view.eks.button.IExtraButton
import io.neoterm.view.eks.button.StatedControlButton

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
class ScrollExtraKeysView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var builtinExtraKeys: MutableList<IExtraButton>? = null
    private var userDefinedExtraKeys: MutableList<IExtraButton>? = null

    private val lineOne: LinearLayout
    private val lineTwo: LinearLayout
    private var typeface: Typeface? = null

    init {
        gravity = Gravity.TOP
        orientation = LinearLayout.VERTICAL

        val scrollOne = HorizontalScrollView(context)
        val scrollTwo = HorizontalScrollView(context)
        lineOne = initLine(scrollOne)
        lineTwo = initLine(scrollTwo)
        addView(scrollOne)
        addView(scrollTwo)

        loadBuiltinKeys()
        loadUserKeys()
        updateButtons()
    }

    private fun initLine(scroll: HorizontalScrollView): LinearLayout {
        val line = LinearLayout(context)
        line.gravity = Gravity.START
        line.orientation = LinearLayout.HORIZONTAL

        scroll.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        scroll.isFillViewport = true
        scroll.isHorizontalScrollBarEnabled = false
        scroll.addView(line)
        return line
    }

    fun readControlButton(): Boolean {
        return CTRL.readState()
    }

    fun readAltButton(): Boolean {
        return ALT.readState()
    }

    fun addUserKey(button: IExtraButton) {
        addKeyButton(userDefinedExtraKeys, button)
    }

    fun addBuiltinKey(button: IExtraButton) {
        addKeyButton(builtinExtraKeys, button)
    }

    fun clearUserKeys() {
        userDefinedExtraKeys!!.clear()
    }

    fun loadUserKeys() {
        userDefinedExtraKeys = ArrayList<IExtraButton>(8)
        val defaultFile = File(NeoTermPath.EKS_DEFAULT_FILE)
        if (!defaultFile.exists()) {
            generateDefaultFile(defaultFile)
        }

        clearUserKeys()
        try {
            val parser = EksConfigParser()
            parser.setInput(defaultFile)
            val config = parser.parse()
            userDefinedExtraKeys!!.addAll(config.shortcutKeys)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadBuiltinKeys() {
        builtinExtraKeys = ArrayList<IExtraButton>(7)
        builtinExtraKeys!!.clear()
        builtinExtraKeys!!.add(ESC)
        builtinExtraKeys!!.add(CTRL)
        builtinExtraKeys!!.add(ALT)
        builtinExtraKeys!!.add(TAB)
        builtinExtraKeys!!.add(ARROW_UP)
        builtinExtraKeys!!.add(ARROW_DOWN)
        builtinExtraKeys!!.add(ARROW_LEFT)
        builtinExtraKeys!!.add(ARROW_RIGHT)
        builtinExtraKeys!!.add(PAGE_UP)
        builtinExtraKeys!!.add(PAGE_DOWN)
        builtinExtraKeys!!.add(HOME)
        builtinExtraKeys!!.add(END)
    }

    fun updateButtons() {
        lineOne.removeAllViews()
        lineTwo.removeAllViews()
        for (extraButton in userDefinedExtraKeys!!) {
            addKeyButton(lineOne, extraButton)
        }
        for (extraButton in builtinExtraKeys!!) {
            addKeyButton(lineTwo, extraButton)
        }
    }

    fun setTextColor(textColor: Int) {
        NORMAL_TEXT_COLOR = textColor
        updateButtons()
    }

    fun setTypeface(typeface: Typeface?) {
        this.typeface = typeface
        updateButtons()
    }

    private fun addKeyButton(buttons: MutableList<IExtraButton>?, button: IExtraButton) {
        if (buttons != null && !buttons.contains(button)) {
            buttons.add(button)
        }
    }

    private fun addKeyButton(contentView: LinearLayout, extraButton: IExtraButton) {
        val button: Button
        if (extraButton is StatedControlButton) {
            val btn = extraButton
            val toggleButton = ToggleButton(context, null, android.R.attr.buttonBarButtonStyle)
            btn.toggleButton = toggleButton
            button = toggleButton

            button.setClickable(true)
            if (btn.initState) {
                btn.toggleButton!!.isChecked = true
                btn.toggleButton!!.setTextColor(SELECTED_TEXT_COLOR)
            }
        } else {
            button = Button(context, null, android.R.attr.buttonBarButtonStyle)
        }

        button.typeface = typeface
        button.text = extraButton.buttonText
        button.setTextColor(NORMAL_TEXT_COLOR)
        button.setAllCaps(false)

        button.setOnClickListener {
            button.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val root = rootView
            extraButton.onClick(root)
        }
        contentView.addView(button)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        val CTRL = StatedControlButton(IExtraButton.KEY_CTRL)
        @SuppressLint("StaticFieldLeak")
        val ALT = StatedControlButton(IExtraButton.KEY_ALT)
        val ESC = ControlButton(IExtraButton.KEY_ESC)
        val TAB = ControlButton(IExtraButton.KEY_TAB)
        val PAGE_UP = ControlButton(IExtraButton.KEY_PAGE_UP)
        val PAGE_DOWN = ControlButton(IExtraButton.KEY_PAGE_DOWN)
        val HOME = ControlButton(IExtraButton.KEY_HOME)
        val END = ControlButton(IExtraButton.KEY_END)
        val ARROW_UP = ControlButton(IExtraButton.KEY_ARROW_UP)
        val ARROW_DOWN = ControlButton(IExtraButton.KEY_ARROW_DOWN)
        val ARROW_LEFT = ControlButton(IExtraButton.KEY_ARROW_LEFT)
        val ARROW_RIGHT = ControlButton(IExtraButton.KEY_ARROW_RIGHT)

        val DEFAULT_FILE_CONTENT = "version " + EksConfigParser.PARSER_VERSION + "\n" +
                "program default\n" +
                "define - false\n" +
                "define / false\n" +
                "define | false\n" +
                "define $ false\n" +
                "define < false\n" +
                "define > false\n"

        var NORMAL_TEXT_COLOR = 0xFFFFFFFF.toInt()
        var SELECTED_TEXT_COLOR = 0xFF80DEEA.toInt()

        private fun generateDefaultFile(defaultFile: File) {
            FileUtils.writeFile(defaultFile, DEFAULT_FILE_CONTENT.toByteArray())
        }
    }
}
