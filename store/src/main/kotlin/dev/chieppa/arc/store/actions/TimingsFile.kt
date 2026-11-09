package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.timing.TimingHistory
import dev.chieppa.arc.core.actions.timing.TimingKey
import dev.chieppa.arc.core.actions.timing.TimingSample
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Path

/** The step timings of each job's last successful runs, kept in a JSON file at [path]. */
class TimingsFile(private val path: Path) {
    fun load(): TimingHistory {
        val stored = StoreJson.load(path, StoredTimings.serializer()) ?: return TimingHistory()
        return TimingHistory(
            stored.jobs.mapNotNull { (key, samples) ->
                TimingKey.parse(key)?.let { it to samples.map { s -> TimingSample(s.run, s.queued, s.steps) }.takeLast(TimingHistory.LIMIT) }
            }.toMap(),
        )
    }

    fun save(history: TimingHistory) {
        val stored = StoredTimings(history.jobs.map { (key, samples) -> key.toString() to samples.map { StoredSample(it.runId, it.queuedSeconds, it.steps) } }.toMap())
        StoreJson.save(path, StoredTimings.serializer(), stored)
    }
}
