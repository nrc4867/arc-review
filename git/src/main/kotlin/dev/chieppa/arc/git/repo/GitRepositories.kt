package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.GitObjects
import dev.chieppa.arc.git.process.GitRunner
import java.io.Closeable
import java.nio.file.Path

/** Every repo Arc reads, opened on first use from where [locator] says each one is, and kept open until [close]. */
class GitRepositories(private val locator: RepoLocator, private val runner: GitRunner = GitRunner()) : GitObjects, Closeable {
    private val open = mutableMapOf<Pair<RepoName, Path>, GitRepository>()

    /** [repo]'s git data. Throws [UnknownRepoException] when the locator does not know it. */
    fun repository(repo: RepoName): GitRepository {
        val dir = locator.locate(repo) ?: throw UnknownRepoException(repo)
        return synchronized(open) { open.getOrPut(repo to dir) { GitRepository(dir, runner) } }
    }

    override suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray? =
        repository(repo).readFile(revision.value, path)

    override fun close() {
        synchronized(open) {
            open.values.forEach(GitRepository::close)
            open.clear()
        }
    }
}
