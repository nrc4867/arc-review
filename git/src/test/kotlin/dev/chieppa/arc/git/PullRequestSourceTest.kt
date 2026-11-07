package dev.chieppa.arc.git

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.PullRequestRefs
import dev.chieppa.arc.git.source.SourceNotFoundException
import dev.chieppa.arc.git.source.SourceReader
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PullRequestSourceTest {
    private val origin = TestRepo()
    private val start = origin.commit("Start", mapOf("app/Main.kt" to lines(5)))
    private val feature: dev.chieppa.arc.core.change.ObjectId

    init {
        origin.git("checkout", "-q", "-b", "feature")
        feature = origin.commit("Feature", mapOf("app/Feature.kt" to "new\n"))
        origin.git("update-ref", "refs/pull/7/head", feature.value)
        origin.git("checkout", "-q", "main")
        origin.commit("Main moves", mapOf("app/Main.kt" to lines(6)))
        origin.git("branch", "-q", "-D", "feature")
    }

    private val cloneDir = Files.createTempDirectory("arc-git-clone").resolve("clone")

    init {
        origin.git("clone", "-q", origin.dir.toString(), cloneDir.toString())
    }

    private val source = Source.PullRequest(RepoName("demo"), 7)

    @Test
    fun aPullRequestHeadIsFetchedIntoAPrivateRefAndComparedFromItsMergeBase() = runTest {
        val repo = GitRepository(cloneDir)
        val lookup = PullRequestLookup { _, _ -> PullRequestRefs("main", headOid = feature.value, title = "Add the feature") }
        val snapshot = SourceReader(repo, lookup).read(source)
        assertEquals("Add the feature", snapshot.title)
        assertEquals(feature, snapshot.revisions.head)
        assertEquals(start, snapshot.revisions.base)
        assertEquals(listOf("app/Feature.kt"), snapshot.files.map { it.path })
        assertEquals(listOf("Feature"), snapshot.commits.map { it.subject })
        assertEquals(feature, repo.resolveCommit("refs/arc/pull/7/head"))
        assertEquals(origin.dir.toString(), repo.remoteUrl())
    }

    @Test
    fun withoutTheHostAPullRequestCannotBeRead() = runTest {
        assertFailsWith<SourceNotFoundException> { SourceReader(GitRepository(cloneDir)).read(source) }
    }
}
