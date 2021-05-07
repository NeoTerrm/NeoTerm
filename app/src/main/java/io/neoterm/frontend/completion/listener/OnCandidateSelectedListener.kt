package io.neoterm.frontend.completion.listener

import io.neoterm.frontend.completion.model.CompletionCandidate

/**
 * @author kiva
 */
interface OnCandidateSelectedListener {
  fun onCandidateSelected(candidate: CompletionCandidate)
}