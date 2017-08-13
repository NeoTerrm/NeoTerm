package io.neoterm.component.completion.provider

import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.frontend.completion.provider.ICandidateProvider
import java.io.File

/**
 * @author kiva
 */

open class FileCompletionProvider : ICandidateProvider {
    override val providerName: String
        get() = "NeoTermProvider.FileCompletionProvider"

    override fun provideCandidates(text: String): List<CompletionCandidate>? {
        var file = File(text)
        var filter: ((File) -> Boolean)? = null

        if (!file.isDirectory) {
            val partName = file.name
            file = file.parentFile
            filter = { pathname -> pathname.name.startsWith(partName) }
        }

        return generateCandidateList(file, filter)
    }

    override fun canComplete(text: String): Boolean {
        return text.startsWith(File.separatorChar)
    }

    private fun listDirectory(path: File, filter: ((File) -> Boolean)?): Array<File> {
        return if (filter != null) path.listFiles(filter) else path.listFiles()
    }

    private fun generateCandidateList(file: File, filter: ((File) -> Boolean)?): List<CompletionCandidate>? {
        if (file.canRead()) {
            val candidates = mutableListOf<CompletionCandidate>()
            listDirectory(file, filter)
                    .mapTo(candidates, {
                        val candidate = CompletionCandidate(it.name)
                        candidate.description = generateDesc(it)
                        candidate.displayName = generateDisplayName(it)
                        candidate
                    })
            return candidates
        }
        return null
    }

    open fun generateDisplayName(file: File): String {
        return if (file.isDirectory) "${file.name}/" else file.name
    }

    open fun generateDesc(file: File): String? {
        return null
    }
}
