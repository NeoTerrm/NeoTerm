package io.neoterm.ui.customize

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import io.neoterm.R
import io.neoterm.component.color.ColorSchemeComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.utils.FileUtils
import io.neoterm.utils.MediaUtils
import java.io.File
import java.io.FileInputStream

/**
 * @author kiva
 */
class CustomizeActivity : BaseCustomizeActivity() {
    val REQUEST_SELECT_FONT = 22222
    val REQUEST_SELECT_COLOR = 22223

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCustomizationComponent(R.layout.ui_customize)

        findViewById<View>(R.id.custom_install_font_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, getString(R.string.install_font)), REQUEST_SELECT_FONT)
        }

        findViewById<View>(R.id.custom_install_color_button).setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(R.string.pref_customization_font)
                    .setNeutralButton(android.R.string.no, null)
                    .setPositiveButton(R.string.install_font, { _, _ ->
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "*/*"
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.install_color)), REQUEST_SELECT_COLOR)
                    })
                    .setNegativeButton(R.string.new_color_scheme, { _, _ ->
                        startActivity(Intent(this, ColorSchemeActivity::class.java))
                    })
                    .show()
        }
    }

    private fun setupSpinners() {
        val fontManager = ComponentManager.getService<FontComponent>()
        val colorSchemeManager = ComponentManager.getService<ColorSchemeComponent>()

        setupSpinner(R.id.custom_font_spinner, fontManager.getFontNames(),
                fontManager.getCurrentFontName(), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val fontName = parent!!.adapter!!.getItem(position) as String
                val typeface = fontManager.getFont(fontName).getTypeFace()
                terminalView.setTypeface(typeface)
                extraKeysView.setTypeface(typeface)
                fontManager.setCurrentFont(fontName)
            }
        })

        setupSpinner(R.id.custom_color_spinner, colorSchemeManager.getColorNames(),
                colorSchemeManager.getCurrentColorName(), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val colorName = parent!!.adapter!!.getItem(position) as String
                val color = colorSchemeManager.getColor(colorName)
                colorSchemeManager.applyColorScheme(terminalView, extraKeysView, color)
                colorSchemeManager.setCurrentColor(colorName)
            }
        })
    }

    private fun setupSpinner(id: Int, data: List<String>, selected: String, listener: AdapterView.OnItemSelectedListener) {
        val spinner = findViewById<Spinner>(id)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
        spinner.setSelection(if (data.contains(selected)) data.indexOf(selected) else 0)
    }

    override fun onResume() {
        super.onResume()
        setupSpinners()
    }

    override fun onDestroy() {
        super.onDestroy()
        session.finishIfRunning()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selected = MediaUtils.getPath(this, data.data)
            if (selected != null && selected.isNotEmpty()) {
                when (requestCode) {
                    REQUEST_SELECT_FONT -> installFont(selected)
                    REQUEST_SELECT_COLOR -> installColor(selected)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun installColor(selected: String) {
        installFileTo(selected, NeoTermPath.COLORS_PATH)
        setupSpinners()
    }

    private fun installFont(selected: String) {
        installFileTo(selected, NeoTermPath.FONT_PATH)
        setupSpinners()
    }

    private fun installFileTo(file: String, targetDir: String) {
        try {
            val fileObject = File(file)
            val input = FileInputStream(fileObject.absolutePath)
            val targetFile = File(targetDir, fileObject.name)
            input.use {
                FileUtils.writeFile(targetFile, it)
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error) + ": ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}