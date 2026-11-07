package dev.chieppa.arc.git

import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.repo.GitCloner
import dev.chieppa.arc.git.repo.GitRepository
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitClonerTest {
    private val origin = TestRepo()
    private val head = origin.commit("First", mapOf("a.txt" to "one\n"))

    init {
        origin.git("config", "uploadpack.allowFilter", "true")
    }

    @Test
    fun aCloneIsBloblessWithNoCheckout() = runTest {
        val target = Files.createTempDirectory("arc-cloner").resolve("owner").resolve("name")
        GitCloner().clone(origin.dir.toUri().toString(), target)
        assertFalse(target.resolve("a.txt").exists())
        GitRepository(target).use { git ->
            assertTrue(git.isPartial())
            assertEquals(head.value, git.resolveCommit("HEAD")?.value)
        }
    }

    @Test
    fun aFailedCloneLeavesNoFolder() = runTest {
        val target = Files.createTempDirectory("arc-cloner").resolve("missing")
        assertFailsWith<GitException> { GitCloner().clone(target.resolveSibling("nowhere").toUri().toString(), target) }
        assertFalse(target.exists())
    }
}
