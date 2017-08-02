package io.neolang.main

import io.neolang.parser.NeoLangParser
import java.io.FileInputStream

/**
 * @author kiva
 */
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                println("Usage: NeoLang <program.nl>")
                return
            }

            val parser = NeoLangParser()
            args.forEach {
                val programCode = readFully(it)
                parser.setInputSource(programCode)
                val ast = parser.parse()

                val visitor = ast.visit().getVisitor(DisplayAstVisitor::class.java)
                if (visitor != null) {
                    println("Compile `$it' -> $ast")
                    visitor.start()
                }
            }
            return
        }

        private fun readFully(file: String): String {
            FileInputStream(file).use {
                val bytes = ByteArray(it.available())
                it.read(bytes)
                return String(bytes)
            }
        }
    }
}