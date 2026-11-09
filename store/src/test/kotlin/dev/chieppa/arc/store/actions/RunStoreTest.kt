package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.actions.WatchedWorkflow
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.log.KeptLine
import dev.chieppa.arc.core.actions.log.KeptLog
import dev.chieppa.arc.core.actions.log.LogSilence
import dev.chieppa.arc.core.actions.timing.TimingHistory
import dev.chieppa.arc.core.actions.timing.TimingKey
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RunStoreTest {
    private val dir: Path = createTempDirectory("arc-runs")
    private val t0 = Instant.parse("2026-11-09T09:00:00Z")
    private val run = WorkflowRun(
        "Lumen/API", 2291, 41, "Deploy", ".github/workflows/deploy.yml", "Session list", 2291, 1, "push", RunStatus.Completed,
        RunConclusion.Failure, "main", "7d41e0a", "jpark", "jpark", t0, t0, t0.plusSeconds(60), listOf(405), "lumen", "https://x",
    )

    @Test
    fun `the watchlist reads back as written`() {
        val file = RunWatchlistFile(dir.resolve("runs.json"))
        assertFalse(file.exists())
        val watchlist = RunWatchlist.seeds("Mine", "Waiting on me")
            .withRule(MineRule.Watched, false)
            .withNotify(RunNotify.MineSucceeded, true)
            .watchingWorkflow(WatchedWorkflow("lumen/platform", ".github/workflows/nightly.yml"), true)
            .watchingRun(RunRef("lumen/api", 7), true, 99)
            .withSummaryArtifact("lumen/api", "report")
        file.save(watchlist)
        assertEquals(watchlist, file.load())
    }

    @Test
    fun `timings read back by their key`() {
        val file = TimingsFile(dir.resolve("timings.json"))
        val key = TimingKey("lumen/api", ".github/workflows/ci.yml", "build")
        val job = RunJob(1, 1, "build", RunStatus.Completed, RunConclusion.Success, t0, t0.plusSeconds(3), t0.plusSeconds(60), listOf(RunStep(1, "Compile", RunStatus.Completed, RunConclusion.Success, t0, t0.plusSeconds(50))))
        val history = TimingHistory().record(key, 1, job)
        file.save(history)
        assertEquals(50L, file.load().stepP90(key, "Compile"))
    }

    @Test
    fun `a kept run reads back and old ones are pruned`() {
        val archive = RunArchive(dir.resolve("runs"))
        val log = KeptLog(listOf(KeptLine(4, 2, 100, "##[error]boom")), 10, 400, listOf(LogSilence(4, 90)))
        val record = RunRecord(run, logs = mapOf(501L to log), seenAt = 1000)
        archive.save(record)
        assertEquals(record, archive.load(run.ref))
        archive.prune(emptySet(), now = 1000 + RunArchive.KEEP_MILLIS - 1)
        assertTrue(archive.load(run.ref) != null)
        archive.prune(setOf(RunRef("lumen/api", 2291)), now = 1000 + RunArchive.KEEP_MILLIS + 1)
        assertTrue(archive.load(run.ref) != null, "kept while in a filter")
        archive.prune(emptySet(), now = 1000 + RunArchive.KEEP_MILLIS + 1)
        assertNull(archive.load(run.ref))
    }

    @Test
    fun `the artifact cache drops the least recently used past its cap`() {
        var now = 1000L
        val cache = ArtifactCache(dir.resolve("artifacts"), capBytes = 10, clock = { now })
        val ref = RunRef("lumen/api", 1)
        cache.put(ref, 1, ByteArray(6).inputStream())
        now += 1000
        cache.put(ref, 2, ByteArray(3).inputStream())
        now += 1000
        cache.get(ref, 1)
        now += 1000
        val third = cache.put(ref, 3, ByteArray(4).inputStream())
        assertTrue(third.exists())
        assertNull(cache.get(ref, 2))
        assertTrue(cache.get(ref, 1) != null)
        assertEquals(10L, cache.size())
        assertFalse(Files.list(dir.resolve("artifacts/lumen/api")).use { s -> s.anyMatch { it.toString().endsWith(".part") } })
    }
}
