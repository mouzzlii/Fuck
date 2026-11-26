package de.trakuna.app

/**
 * Confirms a camera candidate only after it appears in the required number
 * of consecutive analysis frames without another distinct candidate.
 */
internal class ScanCandidateConfirmation(
    private val requiredFrames: Int = 12
) {
    private var lastCandidate: String? = null
    private var consecutiveFrames = 0

    init {
        require(requiredFrames > 0) { "Mindestens ein Bestätigungsbild ist erforderlich." }
    }

    @Synchronized
    fun observe(candidates: Collection<String>): String? {
        val distinctCandidates = candidates.filter { it.isNotBlank() }.distinct()
        if (distinctCandidates.size != 1) {
            reset()
            return null
        }

        val candidate = distinctCandidates.single()
        if (candidate == lastCandidate) {
            consecutiveFrames++
        } else {
            lastCandidate = candidate
            consecutiveFrames = 1
        }
        return candidate.takeIf { consecutiveFrames >= requiredFrames }
    }

    private fun reset() {
        lastCandidate = null
        consecutiveFrames = 0
    }
}
