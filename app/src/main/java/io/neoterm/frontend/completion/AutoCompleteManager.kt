package io.neoterm.frontend.completion

/**
 * @author kiva
 */
object AutoCompleteManager {
    private val completeCandidates = mutableListOf<CompleteCandidate>()

    init {
        val programs = arrayOf("ls", "clean", "exit", "apt", "neoterm-normalize-binary", "less", "ln", "lsof")
        val desc = arrayOf("List files and directories",
                "Clear screen",
                "Exit current executablePath",
                "Installing, Updating, Upgrading packages",
                "Fix program error caused by linux shebang",
                "View files",
                "Create symlinks or hardlinks",
                "List opened files")
        for (i in programs.indices) {
            val candidate = CompleteCandidate(programs[i])
            candidate.description = desc[i]
            completeCandidates.add(candidate)
        }
    }

    fun filter(text: String) : List<CompleteCandidate> {
        val result = mutableListOf<CompleteCandidate>()
        if (text.isNotEmpty()) {
            completeCandidates.forEach {
                if (it.completeString.startsWith(text, ignoreCase = true)) {
                    result.add(it)
                }
            }
        }
        return result
    }
}