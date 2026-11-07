package dev.chieppa.arc.git.source

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository

/** Asks the host about a pull request of a repo; null when the host does not know it or cannot be asked. */
fun interface PullRequestLookup {
    suspend fun refs(repo: GitRepository, source: Source.PullRequest): PullRequestRefs?

    companion object {
        /** Knows no pull request, so none can be read. */
        val NONE = PullRequestLookup { _, _ -> null }
    }
}
