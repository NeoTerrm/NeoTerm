package com.termux

import com.termux.frontend.terminal.eks.button.IExtraButton
import com.termux.frontend.terminal.eks.combine.CombinedSequence
import org.junit.Test

/**
 * @author Sam
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
