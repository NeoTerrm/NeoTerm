package io.neoterm.ui.customize

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.customize.color.ColorSchemeService
import io.neoterm.frontend.preference.NeoTermPath
import io.neoterm.customize.font.FontService
import io.neoterm.frontend.shell.ShellParameter
import io.neoterm.frontend.service.ServiceManager
import io.neoterm.utils.FileUtils
import io.neoterm.utils.MediaUtils
import io.neoterm.utils.TerminalUtils
import io.neoterm.frontend.tinyclient.BasicSessionCallback
import io.neoterm.frontend.tinyclient.BasicViewClient
import io.neoterm.view.eks.ExtraKeysView
import io.neoterm.view.TerminalView
import java.io.File
import java.io.FileInputStream

/**
 * @author kiva
 */
class CustomizeActivity : AppCompatActivity() {
    lateinit var terminalView: TerminalView
    lateinit var viewClient: BasicViewClient
    lateinit var sessionCallback: BasicSessionCallback
    lateinit var session: TerminalSession
    lateinit var extraKeysView: ExtraKeysView

    val REQUEST_SELECT_FONT = 22222
    val REQUEST_SELECT_COLOR = 22223

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_customize)

        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        terminalView = findViewById<TerminalView>(R.id.terminal_view)
        extraKeysView = findViewById<ExtraKeysView>(R.id.custom_extra_keys)
        viewClient = BasicViewClient(terminalView)
        sessionCallback = BasicSessionCallback(terminalView)
        TerminalUtils.setupTerminalView(terminalView, viewClient)

        val parameter = ShellParameter()
                .executablePath("${NeoTermPath.USR_PATH}/bin/applets/echo")
                .arguments(arrayOf("echo", "Hello NeoTerm."))
                .callback(sessionCallback)
                .systemShell(false)

        session = TerminalUtils.createShellSession(this, parameter)
        terminalView.attachSession(session)

        findViewById<View>(R.id.custom_install_font_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, getString(R.string.install_font)), REQUEST_SELECT_FONT)
        }

        findViewById<View>(R.id.custom_install_color_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, getString(R.string.install_color)), REQUEST_SELECT_COLOR)
        }
    }

    private fun setupSpinners() {
        val fontManager = ServiceManager.getService<FontService>()
        val colorSchemeManager = ServiceManager.getService<ColorSchemeService>()

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