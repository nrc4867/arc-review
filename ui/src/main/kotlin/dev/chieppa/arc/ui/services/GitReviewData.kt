package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.marks.MarkReplayer
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.repo.UnknownRepoException
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.SourceReader
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.nio.file.Files
import java.nio.file.Path

/** [ReviewData] from the repos [repos] knows, read through the git CLI, with pull requests asked of GitHub through [pulls]. */
class GitReviewData(private val repos: GitRepositories, private val pulls: GitHubPulls? = null) : ReviewData {
    override suspend fun read(source: Source): SourceSnapshot = reader(source).read(source)

    override suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot = reader(source).readCommit(source, commit)

    override fun pullRequest(source: Source.PullRequest): PullRequestSummary? = pulls?.known(source)

    override suspend fun gitHubRepo(repo: RepoName): GitHubRepo? = try {
        pulls?.gitHubRepo(repos.repository(repo))
    } catch (_: UnknownRepoException) {
        null
    }

    private fun reader(source: Source) = SourceReader(repos.repository(source.repo), pulls ?: PullRequestLookup.NONE)

    override suspend fun replay(repo: RepoName, reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome? =
        MarkReplayer(repos.repository(repo)).replay(reviewed, now)

    override suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray? =
        repos.readFile(repo, revision, path)

    override suspend fun listFiles(repo: RepoName, revision: Revision): List<String> =
        repos.repository(repo).listFiles(revision.value)

    override suspend fun isRepo(dir: Path): Boolean {
        if (!Files.isDirectory(dir)) return false
        return GitRepository(dir).use { it.resolveCommit(HEAD) != null }
    }

    private companion object {
        const val HEAD = "HEAD"
    }
}
