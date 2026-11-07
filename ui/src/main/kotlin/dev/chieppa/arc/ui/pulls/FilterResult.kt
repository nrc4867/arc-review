package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** What a search found when it was last polled: [total] matches in all, the [pulls] that came back, and when, in epoch milliseconds. */
data class FilterResult(val total: Int, val pulls: List<PullRequestSummary>, val fetchedAt: Long)
