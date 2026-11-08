package dev.chieppa.arc.core.conversation

/** The verdict and summary text to send with a source's staged batch. */
data class ReviewSummary(val verdict: Verdict = Verdict.Comment, val body: String = "")
