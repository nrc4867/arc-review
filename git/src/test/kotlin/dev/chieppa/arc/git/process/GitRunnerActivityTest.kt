package dev.chieppa.arc.git.process

import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.git.TestRepo
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitRunnerActivityTest {
    private val repo = TestRepo().apply { commit("First", mapOf("a.txt" to "one\n")) }
    private val log = ActivityLog()

    @Test
    fun `a command that succeeds is reported with its command line and folder`() = runTest {
        GitRunner(activity = log).run(repo.dir, listOf("rev-parse", "HEAD"))
        val entry = log.entries().single()
        assertEquals("git rev-parse HEAD", entry.title)
        assertEquals(repo.dir.toString(), entry.place)
        assertEquals(ActivityOutcome.Succeeded, entry.outcome)
        assertNull(entry.error)
    }

    @Test
    fun `a command that fails is reported with its exit code and standard error`() = runTest {
        val missing = Files.createTempDirectory("arc-runner").resolve("nowhere").toUri().toString()
        assertFailsWith<GitException> { GitRunner(activity = log).run(repo.dir, listOf("ls-remote", missing)) }
        val entry = log.entries().single()
        assertEquals(ActivityOutcome.Failed, entry.outcome)
        assertEquals("exit 128", entry.code)
        assertTrue(entry.error!!.contains("nowhere"), entry.error)
    }

    @Test
    fun `an exit code the caller accepts is a success`() = runTest {
        GitRunner(activity = log).run(repo.dir, listOf("diff", "--quiet", "HEAD", "HEAD"), accept = setOf(0, 1))
        assertEquals(ActivityOutcome.Succeeded, log.entries().single().outcome)
    }

    @Test
    fun `a long running process is reported once as started`() {
        GitRunner(activity = log).start(repo.dir, listOf("cat-file", "--batch")).apply { outputStream.close(); waitFor() }
        val entry = log.entries().single()
        assertEquals(ActivityOutcome.Started, entry.outcome)
        assertEquals("git cat-file --batch", entry.title)
    }

    @Test
    fun `a fetch with a token keeps the token out of the command line and the log`() = runTest {
        val token = "ghp_" + "Z9".repeat(18)
        val clone = TestRepo()
        GitRunner(activity = log, credentials = { token }).run(clone.dir, listOf("fetch", "--quiet", repo.dir.toUri().toString()))
        val entry = log.entries().single()
        assertEquals(ActivityOutcome.Succeeded, entry.outcome)
        assertFalse(token in entry.title)
        assertFalse(GitAuth.TOKEN_VARIABLE in entry.title)
    }
}
