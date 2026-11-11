package dev.chieppa.arc.plugins.source

import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

/** Says when one file is created, changed or deleted; the flow ends when its collector stops. */
fun interface FileWatcher {
    fun changes(file: Path): Flow<Unit>
}
