package io.neoterm.frontend.completion.provider

import io.neoterm.frontend.completion.model.CompletionCandidate

/**
 * @author kiva
 */

interface ICandidateProvider {
  val providerName: String

  fun provideCandidates(text: String): List<CompletionCandidate>?

  fun canComplete(text: String): Boolean
}
