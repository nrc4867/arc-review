package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.RunRef

/** Where kept runs go between sessions: the data folder in the app, nowhere in tests. */
interface RunKeeper {
    suspend fun load(ref: RunRef): RunRecord?
    suspend fun save(record: RunRecord)

    /** Deletes the runs past their keeping time, keeping [keep] whatever their age. */
    suspend fun prune(keep: Set<RunRef>, now: Long)

    companion object {
        val NONE = object : RunKeeper {
            override suspend fun load(ref: RunRef): RunRecord? = null
            override suspend fun save(record: RunRecord) {}
            override suspend fun prune(keep: Set<RunRef>, now: Long) {}
        }
    }
}
