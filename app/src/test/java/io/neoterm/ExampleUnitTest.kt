package io.neoterm

import io.neoterm.customize.shortcut.ShortcutConfigParser
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun test_config_parser() {
        val parser = ShortcutConfigParser()
        parser.setInput(File("docs/shortcut-key-config.example"))
        val config = parser.parse()
    }

    @Test
    fun test_wchar() {
        println("▲".length)
        println("X".length)
    }
}