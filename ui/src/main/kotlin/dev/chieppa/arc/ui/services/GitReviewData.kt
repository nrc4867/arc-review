package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.change.CommitInfo
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
import dev.chieppa.arc.git.marks.MarkReplayer
import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.repo.GitCloner
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.git.repo.UnknownRepoException
import dev.chieppa.arc.git.revision.PushHistoryLookup
import dev.chieppa.arc.git.revision.RevisionReader
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.SourceReader
import dev.chieppa.arc.git.source.SourceResolver
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.host.github.repo.GitHubRemotes
import java.nio.file.Files
import java.nio.file.Path

/**
 * [ReviewData] from the repos [repos] knows, read through the git CLI, with pull requests, their pushes and the checks
 * on commits asked of GitHub through [pulls]. The app's own clones of GitHub repos go under [clones], as
 * `<owner>/<name>`.
 */
class GitReviewData(
    private val repos: GitRepositories,
    private val pulls: GitHubPulls? = null,
    private val clones: Path? = null,
    private val cloner: GitCloner = GitCloner(),
) : ReviewData {
    override suspend fun read(source: Source): SourceSnapshot = reader(source).read(source)

    override suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot = reader(source).readCommit(source, commit)

    override fun pullRequest(source: Source.PullRequest): PullRequestSummary? = pulls?.known(source)

    override suspend fun gitHubRepo(repo: RepoName): GitHubRepo? = try {
        pulls?.gitHubRepo(repos.repository(repo))
    } catch (_: UnknownRepoException) {
        null
    }

    override suspend fun branches(repo: RepoName): List<LocalBranch> = try {
        repos.repository(repo).branches()
    } catch (_: UnknownRepoException) {
        emptyList()
    }

    override suspend fun recentCommits(repo: RepoName, limit: Int): List<CommitInfo> = try {
        repos.repository(repo).recentCommits(limit)
    } catch (_: UnknownRepoException) {
        emptyList()
    }

    override suspend fun findCommit(repo: RepoName, rev: String): CommitInfo? = try {
        val git = repos.repository(repo)
        git.resolveCommit(rev)?.let { git.commit(it) }
    } catch (_: UnknownRepoException) {
        null
    }

    override suspend fun openPulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary>? = pulls?.openPulls(repos, text)

    override suspend fun repoExists(repo: GitHubRepo): Boolean? = pulls?.repoExists(repo)

    private fun reader(source: Source) = SourceReader(repos.repository(source.repo), pulls ?: PullRequestLookup.NONE)

    private fun revisionReader(repo: RepoName): RevisionReader {
        val git = repos.repository(repo)
        return RevisionReader(git, SourceResolver(git, pulls ?: PullRequestLookup.NONE), pulls ?: PushHistoryLookup.NONE)
    }

    override suspend fun revisions(source: Source, recorded: List<RecordedHead>): RevisionHistory =
        revisionReader(source.repo).read(source, recorded)

    override suspend fun checks(repo: RepoName, heads: List<ObjectId>): Map<ObjectId, CheckState> {
        val github = pulls ?: return emptyMap()
        val byOid = heads.associateBy { it.value }
        return github.checks(repos.repository(repo), heads.map { it.value }).mapNotNull { (oid, state) -> byOid[oid]?.let { it to state } }.toMap()
    }

    override suspend fun differingFiles(repo: RepoName, a: SourceRevisions, b: SourceRevisions): Set<String> =
        revisionReader(repo).differing(a, b)

    override suspend fun replay(repo: RepoName, reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome? =
        MarkReplayer(repos.repository(repo)).replay(reviewed, now)

    override suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray? =
        repos.readFile(repo, revision, path)

    override suspend fun listFiles(repo: RepoName, revision: Revision): List<String> =
        repos.repository(repo).listFiles(revision.value)

    override suspend fun clone(repo: GitHubRepo): Path? {
        val target = clones?.resolve(repo.owner)?.resolve(repo.name) ?: return null
        if (isRepo(target)) return target
        return try {
            cloner.clone(GitHubRemotes.url(repo), target)
            target
        } catch (_: GitException) {
            null
        }
    }

    override suspend fun isRepo(dir: Path): Boolean {
        if (!Files.isDirectory(dir)) return false
        return GitRepository(dir).use { it.resolveCommit(HEAD) != null }
    }

    private companion object {
        const val HEAD = "HEAD"
    }
}
