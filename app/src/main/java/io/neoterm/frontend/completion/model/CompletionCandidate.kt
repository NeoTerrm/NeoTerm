package io.neoterm.frontend.completion.model

/**
 * @author kiva
 */
class CompletionCandidate(var completeString: String) {
  var displayName: String = completeString
  var description: String? = null
}