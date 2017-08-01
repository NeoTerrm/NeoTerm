package io.neoterm

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class NeoLangTest {
    @Test
    fun testNeoLangParser() {
        val parser = io.neolang.parser.NeoLangParser()
        parser.setInputSource("app: { x: {} \n x: hello \n a: 1111 \n x: { x: 123 } }")
        val ast = parser.parse()
        println(ast)
    }
}

