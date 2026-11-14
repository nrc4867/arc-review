package dev.chieppa.arc.core.activity

import java.io.IOException
import java.time.Instant
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActivityLogTest {
    private fun entry(title: String, outcome: ActivityOutcome = ActivityOutcome.Succeeded, error: String? = null) =
        ActivityEntry(ActivityKind.Git, title, outcome, Instant.EPOCH, error = error)

    @Test
    fun `keeps only the newest entries up to its capacity`() {
        val log = ActivityLog(capacity = 3)
        (1..5).forEach { log.report(entry("git $it")) }
        assertEquals(listOf("git 3", "git 4", "git 5"), log.entries().map { it.title })
        assertEquals(listOf(3L, 4L, 5L), log.entries().map { it.id })
    }

    @Test
    fun `each channel keeps its own last entries, so a busy one pushes out none of another's`() {
        val log = ActivityLog(capacity = 2)
        val plugin = ActivityChannel.plugin("todos", "TODOs")
        log.report(entry("git 1", ActivityOutcome.Failed, error = "fatal: no"))
        (1..5).forEach { log.report(ActivityEntry(ActivityKind.Output, "line $it", ActivityOutcome.Succeeded, Instant.EPOCH, channel = plugin)) }
        assertEquals(listOf("git 1", "line 4", "line 5"), log.entries().map { it.title })
        assertEquals(listOf("line 4", "line 5"), log.entries(plugin.id).map { it.title })
        assertEquals(listOf(ActivityChannel.GIT, plugin), log.channels())
        log.clear(plugin.id)
        assertEquals(listOf("git 1"), log.entries().map { it.title })
    }

    @Test
    fun `gives the entries after an id`() {
        val log = ActivityLog()
        (1..4).forEach { log.report(entry("git $it")) }
        assertEquals(listOf("git 3", "git 4"), log.since(2).map { it.title })
        assertEquals(emptyList(), log.since(4))
    }

    @Test
    fun `listeners hear each entry until closed`() {
        val log = ActivityLog()
        val heard = mutableListOf<String>()
        val listening = log.listen { heard += it.title }
        log.report(entry("one"))
        listening.close()
        log.report(entry("two"))
        assertEquals(listOf("one"), heard)
    }

    @Test
    fun `clearing keeps the ids counting`() {
        val log = ActivityLog()
        log.report(entry("one"))
        log.clear()
        log.report(entry("two"))
        assertEquals(listOf(2L), log.entries().map { it.id })
    }

    @Test
    fun `a failure is kept with its error text`() {
        val log = ActivityLog()
        assertFailsWith<IOException> {
            log.track(ActivityKind.Update, "Check for updates") { throw IOException("Releases answered 404", IOException("Not Found")) }
        }
        val failed = log.entries().single()
        assertTrue(failed.failed)
        assertEquals("Releases answered 404\nNot Found", failed.error)
        assertTrue(failed.millis!! >= 0)
    }

    @Test
    fun `a success and a cancellation are kept without error`() {
        val log = ActivityLog()
        assertEquals(4, log.track(ActivityKind.Download, "node") { 4 })
        assertFailsWith<CancellationException> { log.track(ActivityKind.Download, "jbr") { throw CancellationException() } }
        assertEquals(listOf(ActivityOutcome.Succeeded, ActivityOutcome.Cancelled), log.entries().map { it.outcome })
        assertFalse(log.entries().any { it.error != null })
    }

    @Test
    fun `secrets are redacted before they are kept`() {
        val log = ActivityLog()
        val token = "ghp_" + "a1".repeat(18)
        log.report(entry("git clone https://x-access-token:$token@github.com/o/n.git", ActivityOutcome.Failed, error = "Authorization: Bearer $token"))
        val kept = log.entries().single()
        assertFalse(token in kept.title || token in kept.error!!)
        assertEquals("git clone https://***@github.com/o/n.git", kept.title)
        assertEquals("Authorization: ***", kept.error)
    }
}
