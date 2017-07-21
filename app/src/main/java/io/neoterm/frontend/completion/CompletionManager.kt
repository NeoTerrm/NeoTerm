package io.neoterm.frontend.completion

import io.neoterm.frontend.completion.model.CompletionResult
import io.neoterm.frontend.completion.provider.ICandidateProvider

/**
 * @author kiva
 */
object CompletionManager {
    private val candidateProviders = mutableMapOf<String, ICandidateProvider>()

    fun registerProvider(provider: ICandidateProvider) {
        this.candidateProviders.put(provider.providerName, provider)
    }

    fun unregisterProvider(providerName: String) {
        this.candidateProviders.remove(providerName)
    }

    fun unregisterProvider(provider: ICandidateProvider) {
        unregisterProvider(provider.providerName)
    }

    fun getProvider(providerName: String): ICandidateProvider? {
        return candidateProviders[providerName]
    }

    fun tryCompleteFor(text: String): CompletionResult {
        val detector = detectProviders(text)
        val provider = detector.detectBest()

        val candidates = provider?.provideCandidates(text) ?: listOf()
        return CompletionResult(candidates, detector)
    }

    private fun detectProviders(text: String): ProviderDetector {
        return ProviderDetector(candidateProviders.values
                .takeWhile { it.canComplete(text) })
    }
}