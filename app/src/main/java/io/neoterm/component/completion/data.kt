package io.neoterm.component.completion

class CompletionCandidate(val completeString: String) {
  var displayName: String = completeString
  var description: String? = null
}

class CompletionResult(val candidates: List<CompletionCandidate>, var scoreMarker: MarkScoreListener) {
  fun markScore(score: Int) {
    scoreMarker.onMarkScore(score)
  }

  fun hasResult(): Boolean {
    return candidates.isNotEmpty()
  }
}

