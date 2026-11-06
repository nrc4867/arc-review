package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.ReplayOutcome

/** Where reading a source from its repo has got to. */
sealed interface SourceLoad {
    data object Loading : SourceLoad

    /** Read: the [snapshot], and the replays of what was reviewed in it, for deciding marks. */
    data class Ready(val snapshot: SourceSnapshot, val replays: List<ReplayOutcome> = emptyList()) : SourceLoad

    /** The source could not be read, such as a commit missing from its repo. */
    data class Failed(val reason: String) : SourceLoad

    /** The snapshot when [this] is ready. */
    val ready: SourceSnapshot? get() = (this as? Ready)?.snapshot
}
