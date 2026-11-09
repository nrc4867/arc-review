package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.actions.WatchedWorkflow
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Files
import java.nio.file.Path

/** The run filters and what makes runs the person's, kept in a JSON file at [path]. Names it does not know are left out. */
class RunWatchlistFile(private val path: Path) {
    fun exists(): Boolean = Files.exists(path)

    fun load(): RunWatchlist {
        val stored = StoreJson.load(path, StoredRunWatchlist.serializer()) ?: return RunWatchlist()
        return RunWatchlist(
            filters = stored.filters.filter { it.id.isNotBlank() }.distinctBy { it.id }.map { RunFilter(it.id, it.name, it.query, it.mine) },
            mineRules = stored.mineRules?.mapNotNull { name -> MineRule.entries.firstOrNull { it.name == name } }?.toSet() ?: MineRule.entries.toSet(),
            workflows = stored.workflows.map { WatchedWorkflow(it.repo, it.path) },
            runs = stored.runs.associate { RunRef(it.repo, it.id) to it.at },
            notify = stored.notify?.mapNotNull { name -> RunNotify.entries.firstOrNull { it.name == name } }?.toSet() ?: RunNotify.DEFAULTS,
            summaryArtifacts = stored.summaryArtifacts,
        )
    }

    fun save(watchlist: RunWatchlist) {
        val stored = StoredRunWatchlist(
            filters = watchlist.filters.map { StoredRunFilter(it.id, it.name, it.query, it.mine) },
            mineRules = watchlist.mineRules.map { it.name }.sorted(),
            workflows = watchlist.workflows.map { StoredWorkflow(it.repo, it.path) },
            runs = watchlist.runs.map { (ref, at) -> StoredWatchedRun(ref.repo, ref.id, at) },
            notify = watchlist.notify.map { it.name }.sorted(),
            summaryArtifacts = watchlist.summaryArtifacts,
        )
        StoreJson.save(path, StoredRunWatchlist.serializer(), stored)
    }
}
