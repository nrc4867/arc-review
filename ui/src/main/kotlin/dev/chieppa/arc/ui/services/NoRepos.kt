package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.UnknownRepoException
import java.nio.file.Path

/** [ReviewData] that knows no repos: every source fails to read and every file is missing. */
object NoRepos : ReviewData {
    override suspend fun read(source: Source): SourceSnapshot = throw UnknownRepoException(source.repo)

    override suspend fun readCommit(source: Source, commit: ObjectId): SourceSnapshot = throw UnknownRepoException(source.repo)

    override suspend fun replay(repo: RepoName, reviewed: ReviewedRevisions, now: SourceRevisions): ReplayOutcome? = null

    override suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray? = null

    override suspend fun listFiles(repo: RepoName, revision: Revision): List<String> = emptyList()

    override suspend fun isRepo(dir: Path): Boolean = false
}
