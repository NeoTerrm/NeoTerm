package io.neoterm.customize.completion

/**
 * @author kiva
 */
class CompleteCandidate(var completeString: String) {
    var displayName: String = completeString
    var description: String? = null
}