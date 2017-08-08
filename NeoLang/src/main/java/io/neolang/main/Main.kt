package io.neolang.main

import io.neolang.parser.NeoLangParser
import io.neolang.visitor.DisplayProcessVisitor
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
                println("Compile `$it'")
                ast.visit()
                        .getVisitor(DisplayProcessVisitor::class.java)
                        ?.start()
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