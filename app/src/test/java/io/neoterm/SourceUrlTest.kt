package io.neoterm

import io.neoterm.component.pm.SourceUtils
import org.junit.Test

/**
 * @author kiva
 */
class SourceUrlTest {
    @Test
    fun testSourceUrl() {
        val url = "http://7sp0th.iok.la:81/neoterm"
        println(SourceUtils.detectSourceFilePrefix(url))
    }
}