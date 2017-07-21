package io.neoterm.customize.completion.provider

import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.frontend.completion.provider.ICandidateProvider
import java.io.File

/**
 * @author kiva
 */

class PathProvider : ICandidateProvider {
    override val providerName: String
        get() = "NeoTermProvider.PathProvider"

    override fun provideCandidates(text: String): List<CompletionCandidate>? {
        var file = File(text)
        if (file.isDirectory) {
            if (file.canRead()) {
                val candidates = mutableListOf<CompletionCandidate>()
                file.listFiles().mapTo(candidates, {
                    CompletionCandidate(it.name)
                })
                return candidates
            }
            return null
        }

        val partName = file.name
        file = file.parentFile
        if (file.canRead()) {
            val candidates = mutableListOf<CompletionCandidate>()
            file.listFiles({ pathname ->  pathname.name.startsWith(partName) })
                    .mapTo(candidates, {
                        CompletionCandidate(it.name)
                    })
            return candidates
        }
        return null
    }

    override fun canComplete(text: String): Boolean {
        return text.startsWith('/')
    }
}
