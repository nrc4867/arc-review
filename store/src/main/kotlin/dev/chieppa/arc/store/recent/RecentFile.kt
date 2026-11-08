package dev.chieppa.arc.store.recent

import dev.chieppa.arc.core.recent.RecentSource
import dev.chieppa.arc.core.recent.RecentSources
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Path

/** The recently opened sources, kept in a JSON file at [path]. A missing file reads as none; an entry with no ref is left out. */
class RecentFile(private val path: Path) {
    fun load(): RecentSources {
        val stored = StoreJson.load(path, StoredRecent.serializer()) ?: return RecentSources()
        val sources = stored.sources.filter { it.ref.isNotBlank() }.distinctBy { it.ref }.map { RecentSource(it.ref, it.title, it.at) }
        return RecentSources(sources.take(RecentSources.LIMIT))
    }

    fun save(recent: RecentSources) {
        StoreJson.save(path, StoredRecent.serializer(), StoredRecent(recent.sources.map { StoredRecentSource(it.ref, it.title, it.at) }))
    }
}
