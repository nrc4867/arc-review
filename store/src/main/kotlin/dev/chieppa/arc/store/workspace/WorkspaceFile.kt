package dev.chieppa.arc.store.workspace

import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Files
import java.nio.file.Path

/** Keeps the workspace in one JSON file at [path]. */
class WorkspaceFile(private val path: Path) {
    /** Whether the file is there; a first run has none. */
    fun exists(): Boolean = Files.exists(path)

    /** The stored workspace, or null when the file is missing or does not read. */
    fun load(): StoredWorkspace? = StoreJson.load(path, StoredWorkspace.serializer())

    fun save(workspace: StoredWorkspace) {
        StoreJson.save(path, StoredWorkspace.serializer(), workspace)
    }
}
