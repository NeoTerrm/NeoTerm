package io.neoterm.view.eks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.neoterm.R
import io.neoterm.customize.eks.EksConfigParser
import io.neoterm.preference.NeoTermPath
import io.neoterm.ui.term.event.ToggleFullScreenEvent
import io.neoterm.ui.term.event.ToggleImeEvent
import io.neoterm.utils.FileUtils
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * A view showing extra keys (such as Escape, Ctrl, Alt) not normally available on an Android soft
 * keyboard.
 */
class ExtraKeysView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val builtinKeys = mutableListOf<ExtraButton>()
    private val userKeys = mutableListOf<ExtraButton>()

    private val buttonBars: MutableList<LinearLayout> = mutableListOf()
    private var typeface: Typeface? = null

    init {
        builtinKeys.add(ESC)
        builtinKeys.add(CTRL)
        builtinKeys.add(TAB)
        builtinKeys.add(PAGE_DOWN)
        builtinKeys.add(ARROW_LEFT)
        builtinKeys.add(ARROW_DOWN)
        builtinKeys.add(ARROW_RIGHT)
        builtinKeys.add(TOGGLE_IME)

        builtinKeys.add(ALT)
        builtinKeys.add(FN)
        builtinKeys.add(TOGGLE_FULL_SCREEN)
        builtinKeys.add(PAGE_UP)
        builtinKeys.add(HOME)
        builtinKeys.add(ARROW_UP)
        builtinKeys.add(END)
        builtinKeys.add(SHOW_ALL_BUTTON)

        gravity = Gravity.TOP
        orientation = LinearLayout.VERTICAL

        loadDefaultUserKeys()
        updateButtons()
    }

    private fun createNewButtonBar(): LinearLayout {
        val line = LinearLayout(context)
        line.gravity = Gravity.START
        line.orientation = LinearLayout.HORIZONTAL
        line.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        return line
    }

    fun readControlButton(): Boolean {
        return CTRL.readState()
    }

    fun readAltButton(): Boolean {
        return ALT.readState()
    }

    fun addUserKey(button: ExtraButton) {
        addKeyButton(userKeys, button)
    }

    fun addBuiltinKey(button: ExtraButton) {
        addKeyButton(builtinKeys, button)
    }

    fun clearUserKeys() {
        userKeys.clear()
    }

    fun loadDefaultUserKeys() {
        val defaultFile = File(NeoTermPath.EKS_DEFAULT_FILE)
        if (!defaultFile.exists()) {
            generateDefaultFile(defaultFile)
        }

        clearUserKeys()
        try {
            val parser = EksConfigParser()
            parser.setInput(defaultFile)
            val config = parser.parse()
            userKeys.addAll(config.shortcutKeys)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateButtons() {
        for (bar in buttonBars) {
            bar.removeAllViews()
        }

        var targetButtonBarIndex = 0
        for ((index, value) in builtinKeys.plus(userKeys).withIndex()) {
            addKeyButton(getButtonBarOrNew(targetButtonBarIndex), value)
            targetButtonBarIndex = (index + 1) / MAX_BUTTONS_PER_LINE
        }
        updateButtonBars()
    }

    private fun updateButtonBars() {
        removeAllViews()
        buttonBars.asReversed()
                .forEachIndexed { index, bar ->
                    if (index <= 1) {
                        bar.visibility = View.GONE
                    }
                    addView(bar)
                }
    }

    private fun getButtonBarOrNew(position: Int): LinearLayout {
        if (position >= buttonBars.size) {
            for (i in 0..(position - buttonBars.size + 1)) {
                buttonBars.add(createNewButtonBar())
            }
        }
        return buttonBars[position]
    }

    fun setTextColor(textColor: Int) {
        NORMAL_TEXT_COLOR = textColor
        updateButtons()
    }

    fun setTypeface(typeface: Typeface?) {
        this.typeface = typeface
        updateButtons()
    }

    private fun addKeyButton(buttons: MutableList<ExtraButton>?, button: ExtraButton) {
        if (buttons != null && !buttons.contains(button)) {
            buttons.add(button)
        }
    }

    private fun addKeyButton(contentView: LinearLayout, extraButton: ExtraButton) {
        val outerButton: Button
        if (extraButton is StatedControlButton) {
            val btn = extraButton
            val toggleButton = ToggleButton(context, null, android.R.attr.buttonBarButtonStyle)
            btn.toggleButton = toggleButton
            outerButton = toggleButton

            outerButton.isClickable = true
            if (btn.initState) {
                btn.toggleButton!!.isChecked = true
                btn.toggleButton!!.setTextColor(SELECTED_TEXT_COLOR)
            }
        } else {
            outerButton = Button(context, null, android.R.attr.buttonBarButtonStyle)
        }

        val param = GridLayout.LayoutParams()
        param.setGravity(Gravity.CENTER)
        param.width = calculateButtonWidth(context)
        param.height = context.resources.getDimensionPixelSize(R.dimen.eks_height_one_line)
        param.topMargin = 0
        param.rightMargin = 0
        param.leftMargin = 0
        param.bottomMargin = 0

        outerButton.layoutParams = param
        outerButton.typeface = typeface
        outerButton.text = extraButton.buttonText
        outerButton.setTextColor(NORMAL_TEXT_COLOR)
        outerButton.setAllCaps(false)

        outerButton.setOnClickListener {
            outerButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val root = rootView
            extraButton.onClick(root)
        }
        contentView.addView(outerButton)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        val CTRL = StatedControlButton(ExtraButton.KEY_CTRL)
        @SuppressLint("StaticFieldLeak")
        val ALT = StatedControlButton(ExtraButton.KEY_ALT)
        val ESC = ControlButton(ExtraButton.KEY_ESC)
        val TAB = ControlButton(ExtraButton.KEY_TAB)
        val PAGE_UP = ControlButton(ExtraButton.KEY_PAGE_UP)
        val PAGE_DOWN = ControlButton(ExtraButton.KEY_PAGE_DOWN)
        val HOME = ControlButton(ExtraButton.KEY_HOME)
        val END = ControlButton(ExtraButton.KEY_END)
        val ARROW_UP = ControlButton(ExtraButton.KEY_ARROW_UP)
        val ARROW_DOWN = ControlButton(ExtraButton.KEY_ARROW_DOWN)
        val ARROW_LEFT = ControlButton(ExtraButton.KEY_ARROW_LEFT)
        val ARROW_RIGHT = ControlButton(ExtraButton.KEY_ARROW_RIGHT)

        val SHOW_ALL_BUTTON = object : TextButton("All") {
            override fun onClick(view: View) {
            }
        }
        val FN = object : ControlButton(ExtraButton.KEY_FN) {
            override fun onClick(view: View) {
            }
        }
        val TOGGLE_FULL_SCREEN = object : ControlButton(ExtraButton.KEY_TOGGLE_FULL_SCREEN) {
            override fun onClick(view: View) {
                EventBus.getDefault().post(ToggleFullScreenEvent())
            }
        }
        val TOGGLE_IME = object : ControlButton(ExtraButton.KEY_TOGGLE_IME) {
            override fun onClick(view: View) {
                EventBus.getDefault().post(ToggleImeEvent())
            }
        }

        val MAX_BUTTONS_PER_LINE = 8

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

        private fun calculateButtonWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels / MAX_BUTTONS_PER_LINE
        }
    }
}
