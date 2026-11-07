package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.services.ReviewData
import kotlinx.coroutines.CancellationException

/**
 * Reads what a [PastedTarget] points at from its clone. A commit in a pull request is read after the pull request,
 * whose head brings the commit into the clone when it was not there, and is placed among its commits.
 */
object LinkFactsReader {
    suspend fun read(data: ReviewData, target: PastedTarget): LinkFacts {
        val source = target.source ?: return LinkFacts()
        val pullRequest = target.pullRequest
        var snapshot = attempt { data.read(source) }
        val pullSnapshot = if (target.withinPull && pullRequest != null) attempt { data.read(pullRequest) } else null
        if (snapshot == null && pullSnapshot != null) snapshot = attempt { data.read(source) }
        val pull = pullRequest?.let(data::pullRequest)
        val last = snapshot?.commits?.lastOrNull()
        val position = (source as? Source.Commit)?.let { commit ->
            pullSnapshot?.commits?.indexOfFirst { it.id.value.startsWith(commit.sha.value) }?.takeIf { it >= 0 }?.plus(1)
        }
        val isPull = source is Source.PullRequest
        return LinkFacts(
            title = (if (isPull) pull?.title else null) ?: snapshot?.title,
            author = (if (isPull) pull?.author else null) ?: last?.author,
            time = last?.committed,
            files = snapshot?.files?.size,
            added = snapshot?.files?.sumOf { it.added ?: 0 } ?: 0,
            removed = snapshot?.files?.sumOf { it.removed ?: 0 } ?: 0,
            commits = snapshot?.commits?.size,
            position = position,
            of = position?.let { pullSnapshot?.commits?.size },
            pull = pull,
        )
    }

    private suspend fun attempt(read: suspend () -> SourceSnapshot): SourceSnapshot? = try {
        read()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
}
