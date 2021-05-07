package io.neoterm.ui.customize

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import io.neoterm.R
import io.neoterm.component.colorscheme.ColorSchemeComponent
import io.neoterm.component.font.FontComponent
import io.neoterm.frontend.component.ComponentManager
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.utils.MediaUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author kiva
 */
class CustomizeActivity : BaseCustomizeActivity() {
  private val REQUEST_SELECT_FONT = 22222
  private val REQUEST_SELECT_COLOR = 22223

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
      val intent = Intent()
      intent.action = Intent.ACTION_GET_CONTENT
      intent.type = "*/*"
      startActivityForResult(
        Intent.createChooser(intent, getString(R.string.install_color)),
        REQUEST_SELECT_COLOR
      )
    }
  }

  private fun setupSpinners() {
    val fontComponent = ComponentManager.getComponent<FontComponent>()
    val colorSchemeComponent = ComponentManager.getComponent<ColorSchemeComponent>()

    setupSpinner(R.id.custom_font_spinner, fontComponent.getFontNames(),
      fontComponent.getCurrentFontName(), object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          val fontName = parent!!.adapter!!.getItem(position) as String
          val font = fontComponent.getFont(fontName)
          fontComponent.applyFont(terminalView, extraKeysView, font)
          fontComponent.setCurrentFont(fontName)
        }
      })

    val colorData = listOf(
      getString(R.string.new_color_scheme),
      *colorSchemeComponent.getColorSchemeNames().toTypedArray()
    )
    setupSpinner(R.id.custom_color_spinner, colorData,
      colorSchemeComponent.getCurrentColorSchemeName(), object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          if (position == 0) {
            val intent = Intent(this@CustomizeActivity, ColorSchemeActivity::class.java)
            startActivity(intent)
            return
          }
          val colorName = parent!!.adapter!!.getItem(position) as String
          val color = colorSchemeComponent.getColorScheme(colorName)
          colorSchemeComponent.applyColorScheme(terminalView, extraKeysView, color)
          colorSchemeComponent.setCurrentColorScheme(colorName)
        }
      })
  }

  private fun setupSpinner(
    id: Int,
    data: List<String>,
    selected: String,
    listener: AdapterView.OnItemSelectedListener
  ): Spinner {
    val spinner = findViewById<Spinner>(id)
    val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter
    spinner.onItemSelectedListener = listener
    spinner.setSelection(if (data.contains(selected)) data.indexOf(selected) else 0)
    return spinner
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
    if (resultCode == RESULT_OK && data != null) {
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
    kotlin.runCatching {
      val source = File(file)
      Files.copy(source.toPath(), Paths.get(targetDir, source.name))
    }.onFailure {
      Toast.makeText(this, getString(R.string.error) + ": ${it.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      android.R.id.home -> finish()
    }
    return super.onOptionsItemSelected(item)
  }
}