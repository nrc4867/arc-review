package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.revision.RevisionHistory
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.nio.file.Path

/** Everything the window reads from repos. Every call runs off the UI thread and stops when its caller is cancelled. */
interface ReviewData {
    /** The whole of [source]. */
    suspend fun read(source: Source): SourceSnapshot

    /** One [commit] of [source], compared with its parent. */
    suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot

    /** What replaying the change reviewed at [reviewed] onto [now] gives, or null when it cannot be replayed. */
    suspend fun replay(repo: RepoName, reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome?

    /** The bytes of [path] in [repo] at [revision], or null when there is no such file. */
    suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray?

    /** Every file path in [repo] at [revision], changed or not. */
    suspend fun listFiles(repo: RepoName, revision: Revision): List<String>

    /** Whether [dir] holds a git repo with at least one commit. */
    suspend fun isRepo(dir: Path): Boolean

    /** What the host said of pull request [source] when it was last read; null before that or with no host. */
    fun pullRequest(source: Source.PullRequest): PullRequestSummary? = null

    /** The branches of [repo]'s clone, newest commit first; none for a repo not known. */
    suspend fun branches(repo: RepoName): List<LocalBranch> = emptyList()

    /** The open pull requests in [repos] whose titles hold [text]; null when the host cannot be asked, as without a token. */
    suspend fun openPulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary>? = null

    /** The GitHub repo [repo]'s clone comes from, or null when it comes from elsewhere. */
    suspend fun gitHubRepo(repo: RepoName): GitHubRepo? = null

    /**
     * The revisions [source] has had: the heads Arc [recorded] reading it, and those its host knows, each pinned in
     * the clone. Heads that cannot be read are left out.
     */
    suspend fun revisions(source: Source, recorded: List<RecordedHead>): RevisionHistory = RevisionHistory(source, emptyList())

    /** The overall checks on each of [heads] in [repo] that the host has checks for. */
    suspend fun checks(repo: RepoName, heads: List<ObjectId>): Map<ObjectId, CheckState> = emptyMap()

    /** The paths in [repo] whose change differs between [a] and [b], each taken from its own base. */
    suspend fun differingFiles(repo: RepoName, a: SourceRevisions, b: SourceRevisions): Set<String> = emptySet()
}
