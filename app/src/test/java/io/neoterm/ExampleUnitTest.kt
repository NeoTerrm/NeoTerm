package io.neoterm

import io.neoterm.customize.pm.NeoPackageManager
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
    @Throws(Exception::class)
    fun test_config_parser() {
        val pm = NeoPackageManager.get()
        pm.refreshPackageList(File("/Users/kiva/1.txt"), true)
        val clang = pm.getPackageInfo("clang")
        println(">>> Parsed ${pm.packageCount} packages.")
    }

    @Test
    fun test_url() {
        val url = URL("https://mirrors.geekpie.org/neoterm")
        val builder = StringBuilder()
        builder.append(url.host)
        builder.append("_")
        if (url.path.isNotEmpty()) {
            builder.append(url.path.substring(1)) // Skip '/'
        }
        builder.append("_dists_stable_main_binary-")
        val packageListFileName = builder.toString()
        println(packageListFileName)
    }
}