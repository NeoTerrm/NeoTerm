package io.neoterm.component.completion

import io.neoterm.component.NeoComponent

class CompletionComponent : NeoComponent {
  override fun onServiceInit() {
    CompletionManager.registerProvider(FileCompletionProvider())
    CompletionManager.registerProvider(ProgramCompletionProvider())
  }

  override fun onServiceDestroy() {
  }

  override fun onServiceObtained() {
  }
}

object CompletionManager {
  private val candidateProviders = mutableMapOf<String, ICandidateProvider>()

  fun registerProvider(provider: ICandidateProvider) {
    this.candidateProviders[provider.providerName] = provider
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

class ProviderDetector(val providers: List<ICandidateProvider>) : MarkScoreListener {
  private var detectedProvider: ICandidateProvider? = null

  override fun onMarkScore(score: Int) {
    // TODO: Save provider score
  }

  fun detectBest(): ICandidateProvider? {
    // TODO: detect best
    detectedProvider = if (providers.isEmpty())
      null
    else
      providers[0]

    return detectedProvider
  }
}

