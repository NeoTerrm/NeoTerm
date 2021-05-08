package io.neoterm.component.config

import io.neolang.runtime.type.NeoLangValue
import io.neolang.visitor.ConfigVisitor
import io.neoterm.component.colorscheme.NeoColorScheme
import io.neoterm.component.extrakey.NeoExtraKey
import io.neoterm.frontend.session.view.extrakey.TextButton
import io.neoterm.utils.NLog
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.*

interface IConfigureLoader {
  fun loadConfigure(): NeoConfigureFile?
}

class NeoLangConfigureLoader(private val configFile: File) : IConfigureLoader {
  override fun loadConfigure(): NeoConfigureFile? {
    val configureFile = NeoConfigureFile(configFile)
    return if (configureFile.parseConfigure()) configureFile else null
  }
}

class OldConfigureLoader(private val configFile: File) : IConfigureLoader {
  override fun loadConfigure(): NeoConfigureFile? {
    return when (configFile.extension) {
      "eks" -> returnConfigure(OldExtraKeysConfigureFile(configFile))
      "color" -> returnConfigure(OldColorSchemeConfigureFile(configFile))
      else -> null
    }
  }

  private fun returnConfigure(configureFile: NeoConfigureFile): NeoConfigureFile? {
    return if (configureFile.parseConfigure()) configureFile else null
  }

  companion object {
    class OldColorSchemeConfigureFile(configureFile: File) : NeoConfigureFile(configureFile) {
      override var configVisitor: ConfigVisitor? = null

      override fun parseConfigure(): Boolean {
        try {
          val visitor = ConfigVisitor()
          visitor.onStart()
          visitor.onEnterContext(NeoColorScheme.CONTEXT_META_NAME)

          visitor.getCurrentContext()
            .defineAttribute(NeoColorScheme.COLOR_META_NAME, NeoLangValue(configureFile.nameWithoutExtension))
            .defineAttribute(NeoColorScheme.COLOR_META_VERSION, NeoLangValue("1.0"))

          visitor.onEnterContext(NeoColorScheme.CONTEXT_COLOR_NAME)

          return FileInputStream(configureFile).use {
            val prop = Properties()
            prop.load(it)
            prop.forEach {
              visitor.getCurrentContext().defineAttribute(it.key as String, NeoLangValue(it.value as String))
            }
            visitor.onFinish()
            this.configVisitor = visitor
            true
          }

        } catch (e: Exception) {
          this.configVisitor = null
          NLog.e("ConfigureLoader", "Error while loading old config", e)
          return false
        }
      }
    }
    class OldExtraKeysConfigureFile(configureFile: File) : NeoConfigureFile(configureFile) {
      override var configVisitor: ConfigVisitor? = null

      override fun parseConfigure(): Boolean {
        try {
          val config = parseOldConfig(BufferedReader(FileReader(configureFile)))
          return generateVisitor(config)
        } catch (e: Exception) {
          NLog.e("ConfigureLoader", "Failed to load old extra keys config: ${e.localizedMessage}")
          return false
        }
      }

      private fun generateVisitor(config: NeoExtraKey): Boolean {
        configVisitor = ConfigVisitor()
        val visitor = configVisitor!!
        visitor.onStart()
        visitor.onEnterContext(NeoExtraKey.EKS_META_CONTEXT_NAME)
        visitor.getCurrentContext()
          .defineAttribute(NeoExtraKey.EKS_META_VERSION, NeoLangValue(config.version))
          .defineAttribute(NeoExtraKey.EKS_META_WITH_DEFAULT, NeoLangValue(config.withDefaultKeys))

        // program
        visitor.onEnterContext(NeoExtraKey.EKS_META_PROGRAM)
        config.programNames.forEachIndexed { index, program ->
          visitor.getCurrentContext().defineAttribute(index.toString(), NeoLangValue(program))
        }
        visitor.onExitContext()

        // key
        visitor.onEnterContext(NeoExtraKey.EKS_META_KEY)
        config.shortcutKeys.forEachIndexed { index, button ->
          if (button is TextButton) {
            visitor.onEnterContext(index.toString())
            visitor.getCurrentContext()
              .defineAttribute(NeoExtraKey.EKS_META_WITH_ENTER, NeoLangValue(button.withEnter))
              .defineAttribute(NeoExtraKey.EKS_META_DISPLAY, NeoLangValue(button.buttonKeys!!))
              .defineAttribute(NeoExtraKey.EKS_META_CODE, NeoLangValue(button.buttonKeys!!))
            visitor.onExitContext()
          }
        }
        visitor.onExitContext()

        visitor.onFinish()
        return true
      }

      private fun parseOldConfig(source: BufferedReader): NeoExtraKey {
        val config = NeoExtraKey()
        var line: String? = source.readLine()

        while (line != null) {
          line = line.trim().trimEnd()
          if (line.isEmpty() || line.startsWith("#")) {
            line = source.readLine()
            continue
          }

          if (line.startsWith(NeoExtraKey.EKS_META_VERSION)) {
            parseHeader(line, config)
          } else if (line.startsWith(NeoExtraKey.EKS_META_PROGRAM)) {
            parseProgram(line, config)
          } else if (line.startsWith("define")) {
            parseKeyDefine(line, config)
          } else if (line.startsWith(NeoExtraKey.EKS_META_WITH_DEFAULT)) {
            parseWithDefault(line, config)
          }
          line = source.readLine()
        }

        if (config.version < 0) {
          throw RuntimeException("Not a valid shortcut config file")
        }
        if (config.programNames.size == 0) {
          throw RuntimeException("At least one program name should be given")
        }
        return config
      }

      private fun parseWithDefault(line: String, config: NeoExtraKey) {
        val value = line.substring(NeoExtraKey.EKS_META_WITH_DEFAULT.length).trim().trimEnd()
        config.withDefaultKeys = value == "true"
      }

      private fun parseKeyDefine(line: String, config: NeoExtraKey) {
        val keyDefine = line.substring("define".length).trim().trimEnd()
        val keyValues = keyDefine.split(" ")
        if (keyValues.size < 2) {
          throw RuntimeException("Bad define")
        }

        val buttonText = keyValues[0]
        val withEnter = keyValues[1] == "true"

        config.shortcutKeys.add(TextButton(buttonText, withEnter))
      }

      private fun parseProgram(line: String, config: NeoExtraKey) {
        val programNames = line.substring(NeoExtraKey.EKS_META_PROGRAM.length).trim().trimEnd()
        if (programNames.isEmpty()) {
          return
        }

        for (name in programNames.split(" ")) {
          config.programNames.add(name)
        }
      }

      private fun parseHeader(line: String, config: NeoExtraKey) {
        val version: Int
        val versionString = line.substring(NeoExtraKey.EKS_META_VERSION.length).trim().trimEnd()
        try {
          version = Integer.parseInt(versionString)
        } catch (e: NumberFormatException) {
          throw RuntimeException("Bad version '$versionString'")
        }

        config.version = version
      }
    }
  }
}
