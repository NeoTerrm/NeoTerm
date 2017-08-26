package io.neoterm

import io.neoterm.frontend.terminal.eks.button.IExtraButton
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
        var chars = "Unchanged"
        val XX = key.keys[0]
        when (IExtraButton.KEY_CTRL) {
            XX -> chars = "Detected ctrl"
        }
        println(chars)
    }
}