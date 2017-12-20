package io.neoterm.ui.customize

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import es.dmoral.coloromatic.ColorOMaticDialog
import es.dmoral.coloromatic.IndicatorMode
import es.dmoral.coloromatic.colormode.ColorMode
import io.neoterm.R
import io.neoterm.backend.TerminalColors
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.colorscheme.NeoColorScheme
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.ui.customize.adapter.ColorItemAdapter
import io.neoterm.ui.customize.model.ColorItem
import io.neoterm.utils.TerminalUtils


/**
 * @author kiva
 */
class ColorSchemeActivity : BaseCustomizeActivity() {
    private val COMPARATOR = SortedListAdapter.ComparatorBuilder<ColorItem>()
            .setOrderForModel<ColorItem>(ColorItem::class.java) { a, b ->
                a.colorType.compareTo(b.colorType)
            }
            .build()

    var changed = false
    private lateinit var editingColorScheme: NeoColorScheme
    lateinit var adapter: ColorItemAdapter

    private val colorSchemeComponent = ComponentManager.getComponent<ColorSchemeComponent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCustomizationComponent(R.layout.ui_color_scheme)

        editingColorScheme = colorSchemeComponent.getCurrentColorScheme().copy()
        editingColorScheme.colorName = ""

        val terminalView = findViewById<TerminalView>(R.id.terminal_view)
        TerminalUtils.setupTerminalView(terminalView, null)

        adapter = ColorItemAdapter(this, editingColorScheme, COMPARATOR, object : ColorItemAdapter.Listener {
            override fun onModelClicked(model: ColorItem) {
                showItemEditor(model)
            }
        })
        val recyclerView = findViewById<RecyclerView>(R.id.custom_color_color_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_color_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_done -> applyColorScheme(editingColorScheme)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event!!.action == KeyEvent.ACTION_DOWN && changed) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.discard_changes))
                    .setPositiveButton(R.string.save, { _, _ ->
                        applyColorScheme(editingColorScheme, true)
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setNeutralButton(R.string.exit, { _, _ ->
                        finish()
                    })
                    .show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showItemEditor(model: ColorItem) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_color, null, false)
        view.findViewById<TextView>(R.id.dialog_edit_color_info).text = getString(R.string.input_new_value)

        val edit = view.findViewById<EditText>(R.id.dialog_edit_color_editor)
        edit.setText(model.colorValue)
        if (model.colorValue.isNotEmpty()) {
            edit.setTextColor(TerminalColors.parse(model.colorValue))
        }
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    val color = TerminalColors.parse(editable.toString())
                    if (color != 0) {
                        edit.setTextColor(color)
                    } else {
                        edit.setTextColor(resources.getColor(R.color.textColor))
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        val applyColor: (newColor: String) -> Unit = { newColor ->
            model.colorValue = newColor
            adapter.notifyItemChanged(adapter.colorList.indexOf(model))

            editingColorScheme.setColor(model.colorType, model.colorValue)
            colorSchemeComponent.applyColorScheme(terminalView, null, editingColorScheme)
            changed = true
        }

        AlertDialog.Builder(this)
                .setTitle(model.colorName)
                .setView(view)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, { _, _ ->
                    applyColor(edit.text.toString());
                })
                .setNeutralButton(R.string.select_new_value, { _, _ ->
                    ColorOMaticDialog.Builder()
                            .initialColor(TerminalColors.parse(model.colorValue))
                            .colorMode(ColorMode.RGB)
                            .indicatorMode(IndicatorMode.HEX)
                            .onColorSelected { newColor ->
                                applyColor("#${Integer.toHexString(newColor).substring(2)}")
                            }
                            .showColorIndicator(true)
                            .create()
                            .show(supportFragmentManager, "ColorOMaticDialog")
                })
                .show()
    }

    private fun applyColorScheme(colorScheme: NeoColorScheme, finishAfter: Boolean = false) {
        if (colorScheme.colorName.isEmpty()) {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null, false)
            view.findViewById<TextView>(R.id.dialog_edit_text_info).text = getString(R.string.save_color_info)

            val edit = view.findViewById<EditText>(R.id.dialog_edit_text_editor)
            edit.setText(getString(R.string.save_color_scheme_name_template))

            AlertDialog.Builder(this)
                    .setTitle(R.string.save_color)
                    .setView(view)
                    .setPositiveButton(android.R.string.yes, { _, _ ->
                        colorScheme.colorName = edit.text.toString()
                        applyColorScheme(colorScheme, finishAfter)
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show()
        } else {
            try {
                colorSchemeComponent.saveColorScheme(colorScheme)
                colorSchemeComponent.reloadColorSchemes()
                colorSchemeComponent.setCurrentColorScheme(colorScheme)
                changed = false

                Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show()
                if (finishAfter) {
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.error) + ": ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}