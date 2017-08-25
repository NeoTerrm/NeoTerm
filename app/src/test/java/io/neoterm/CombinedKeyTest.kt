package io.neoterm

import io.neoterm.frontend.terminal.eks.combine.CombinedSequence
import org.junit.Test

/**
 * @author kiva
 */
class CombinedKeyTest {
    @Test
    fun testCombinedKey() {
        val key = CombinedSequence.solveString("<Ctrl> <Alt> <F1> q")
        println(key.keys)
    }
}