package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.host.github.pulls.PullLabel
import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** One group of a saved filter's pull requests: its [label] when grouped by label, else its [name]; null name for "no label". */
data class PullGroup(val name: String?, val label: PullLabel?, val pulls: List<PullRequestSummary>)
