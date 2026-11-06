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
import dev.chieppa.arc.git.source.SourceReader
import java.nio.file.Files
import java.nio.file.Path

/** [ReviewData] from the repos [repos] knows, read through the git CLI. */
class GitReviewData(private val repos: GitRepositories) : ReviewData {
    override suspend fun read(source: Source): SourceSnapshot = SourceReader(repos.repository(source.repo)).read(source)

    override suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot =
        SourceReader(repos.repository(source.repo)).readCommit(source, commit)

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
