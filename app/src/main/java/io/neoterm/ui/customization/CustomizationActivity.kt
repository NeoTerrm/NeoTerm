package io.neoterm.ui.customization

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.preference.NeoTermPath
import io.neoterm.customize.font.FontManager
import io.neoterm.utils.FileUtils
import io.neoterm.utils.MediaUtils
import io.neoterm.utils.TerminalUtils
import io.neoterm.view.BasicSessionCallback
import io.neoterm.view.BasicViewClient
import io.neoterm.view.ExtraKeysView
import io.neoterm.view.TerminalView
import java.io.File
import java.io.FileInputStream

/**
 * @author kiva
 */
class CustomizationActivity : AppCompatActivity() {
    lateinit var terminalView: TerminalView
    lateinit var viewClient: BasicViewClient
    lateinit var sessionCallback: BasicSessionCallback
    lateinit var session: TerminalSession
    lateinit var extraKeysView: ExtraKeysView

    val REQUEST_SELECT_FONT = 22222
    val REQUEST_SELECT_COLOR = 22223

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_customization)

        // ensure that folders and files are exist
        ColorSchemeManager.init(this)
        FontManager.init(this)

        val toolbar = findViewById(R.id.custom_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terminalView = findViewById(R.id.terminal_view) as TerminalView
        extraKeysView = findViewById(R.id.custom_extra_keys) as ExtraKeysView
        viewClient = BasicViewClient(terminalView)
        sessionCallback = BasicSessionCallback(terminalView)
        TerminalUtils.setupTerminalView(terminalView, viewClient)
        session = TerminalUtils.createSession(this, "${NeoTermPath.USR_PATH}/bin/applets/echo",
                arrayOf("echo", "Hello NeoTerm."), null, null, sessionCallback, false)
        terminalView.attachSession(session)

        findViewById(R.id.custom_install_font_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, getString(R.string.install_font)), REQUEST_SELECT_FONT)
        }

        findViewById(R.id.custom_install_color_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, getString(R.string.install_color)), REQUEST_SELECT_COLOR)
        }
    }

    private fun setupSpinners() {
        FontManager.refreshFontList()
        setupSpinner(R.id.custom_font_spinner, FontManager.getFontNames(),
                FontManager.getCurrentFontName(), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val fontName = parent!!.adapter!!.getItem(position) as String
                val typeface = FontManager.getFont(fontName).getTypeFace()
                terminalView.setTypeface(typeface)
                extraKeysView.setTypeface(typeface)
                FontManager.setCurrentFont(fontName)
            }
        })

        ColorSchemeManager.refreshColorList()
        setupSpinner(R.id.custom_color_spinner, ColorSchemeManager.getColorNames(),
                ColorSchemeManager.getCurrentColorName(), object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val colorName = parent!!.adapter!!.getItem(position) as String
                val color = ColorSchemeManager.getColor(colorName)
                ColorSchemeManager.applyColorScheme(terminalView, extraKeysView, color)
                ColorSchemeManager.setCurrentColor(colorName)
            }
        })
    }

    private fun setupSpinner(id: Int, data: List<String>, selected: String, listener: AdapterView.OnItemSelectedListener) {
        val spinner = findViewById(id) as Spinner
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