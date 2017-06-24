package io.neoterm

import io.neoterm.installer.packages.NeoPackageManager
import org.junit.Test
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
        val pm = NeoPackageManager.getInstance()
        pm.refreshPackageList(File("/Users/kiva/1.txt"))
        val clang = pm.getPackageInfo("clang")
        println(">>> Parsed ${pm.packageCount} packages.")
    }
}