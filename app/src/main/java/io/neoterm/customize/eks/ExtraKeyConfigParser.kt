package io.neoterm.customize.eks

import io.neoterm.view.eks.button.TextButton
import java.io.*

/**
 * @author kiva
 */
class ExtraKeyConfigParser {
    companion object {
        const val PARSER_VERSION = 5
    }

    private lateinit var source: BufferedReader

    fun setInput(file: File) {
        source = BufferedReader(FileReader(file))
    }

    fun setInput(bufferedReader: BufferedReader) {
        source = bufferedReader
    }

    fun setInput(inputStream: BufferedInputStream) {
        source = BufferedReader(InputStreamReader(inputStream))
    }

    fun parse(): NeoExtraKey {
        val config = NeoExtraKey()
        var line: String? = source.readLine()

        while (line != null) {
            line = line.trim().trimEnd()
            if (line.isEmpty() || line.startsWith("#")) {
                line = source.readLine()
                continue
            }

            if (line.startsWith("version")) {
                parseHeader(line, config)
            } else if (line.startsWith("program")) {
                parseProgram(line, config)
            } else if (line.startsWith("define")) {
                parseKeyDefine(line, config)
            } else if (line.startsWith("with-default")) {
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
        val value = line.substring("with-default".length).trim().trimEnd()
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
        val programNames = line.substring("program".length).trim().trimEnd()
        if (programNames.isEmpty()) {
            return
        }

        for (name in programNames.split(" ")) {
            config.programNames.add(name)
        }
    }

    private fun parseHeader(line: String, config: NeoExtraKey) {
        val version: Int
        val versionString = line.substring("version".length).trim().trimEnd()
        try {
            version = Integer.parseInt(versionString)
        } catch (e: NumberFormatException) {
            throw RuntimeException("Bad version '$versionString'")
        }

        if (version > ExtraKeyConfigParser.PARSER_VERSION) {
            throw RuntimeException("Required version: $version, please upgrade your app")
        }

        config.version = version
    }
}