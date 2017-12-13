package io.neoterm.frontend.terminal.extrakey.combine

/**
 * <Ctrl> <Alt> <Delete>
 * <Ctrl> C
 * <Esc> :q <Enter>
 *
 * @author kiva
 */
class CombinedSequence private constructor() {
    val keys = mutableListOf<String>()

    companion object {
        fun solveString(keyString: String): CombinedSequence {
            val seq = CombinedSequence()
            keyString.split(' ').forEach {
                val key = if (it.startsWith('<') && it.endsWith('>')) {
                    // is a sequence
                    it.substring(1, it.length - 1)
                } else {
                    // is a normal string
                    it
                }
                seq.keys.add(key)
            }
            return seq
        }
    }
}