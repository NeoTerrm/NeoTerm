package io.neoterm.component.completion

interface MarkScoreListener {
  fun onMarkScore(score: Int)
}

interface OnAutoCompleteListener {
  fun onCompletionRequired(newText: String?)
  fun onKeyCode(keyCode: Int, keyMod: Int)
  fun onCleanUp()
  fun onFinishCompletion(): Boolean
}

interface OnCandidateSelectedListener {
  fun onCandidateSelected(candidate: CompletionCandidate)
}
