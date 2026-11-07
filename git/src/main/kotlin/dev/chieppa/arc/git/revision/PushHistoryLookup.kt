package dev.chieppa.arc.git.revision

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository

/** Asks the host which heads a pull request of a repo has had; empty when the host does not know or cannot be asked. */
fun interface PushHistoryLookup {
    suspend fun heads(repo: GitRepository, source: Source.PullRequest): List<PushedHead>

    companion object {
        /** Knows no pushes. */
        val NONE = PushHistoryLookup { _, _ -> emptyList() }
    }
}
