package dev.chieppa.arc.plugins.source

import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

/** Says when anything under a folder changes; the flow ends when its collector stops. */
fun interface DirectoryWatcher {
    fun changes(folder: Path): Flow<Unit>

    companion object {
        /** Never says anything changed. */
        val NONE = DirectoryWatcher { kotlinx.coroutines.flow.emptyFlow() }
    }
}
