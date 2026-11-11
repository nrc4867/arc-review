package dev.chieppa.arc.plugins.host

import java.nio.file.Path

/** A worker to start for [plugin], whose files are under [readRoot], the folder the Node process must be able to read. */
data class WorkerSpec(val plugin: String, val readRoot: Path)
