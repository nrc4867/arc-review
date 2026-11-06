package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
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
}
