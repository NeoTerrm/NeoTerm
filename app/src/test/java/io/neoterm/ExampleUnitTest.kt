package io.neoterm

import io.neoterm.customize.pm.NeoPackageManager
import io.neoterm.customize.pm.NeoPackageManagerUtils
import org.junit.Test
import java.io.File
import java.net.URL

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun test_pkg_parser() {
        val prefix = NeoPackageManagerUtils.detectSourceFilePrefix("https://baidu.com:81")
        println(prefix)
    }
}

