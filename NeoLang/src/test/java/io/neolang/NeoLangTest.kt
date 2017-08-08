package io.neolang

import io.neolang.main.Main
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class NeoLangTest {
    @Test
    fun arrayTest() {
        Main.main(arrayOf("NeoLang/example/extra-key.nl"))
    }
}

