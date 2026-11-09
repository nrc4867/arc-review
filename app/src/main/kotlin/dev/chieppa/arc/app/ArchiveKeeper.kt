package dev.chieppa.arc.app

import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.store.actions.RunArchive
import dev.chieppa.arc.ui.services.RunKeeper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Kept runs in the data folder's [archive], read and written on [io]. */
class ArchiveKeeper(private val archive: RunArchive, private val io: CoroutineDispatcher = Dispatchers.IO) : RunKeeper {
    override suspend fun load(ref: RunRef): RunRecord? = withContext(io) { archive.load(ref) }

    override suspend fun save(record: RunRecord) {
        withContext(io) { archive.save(record) }
    }

    override suspend fun prune(keep: Set<RunRef>, now: Long) {
        withContext(io) { archive.prune(keep, now) }
    }
}
