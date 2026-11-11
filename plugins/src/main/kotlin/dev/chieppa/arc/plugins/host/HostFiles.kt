package dev.chieppa.arc.plugins.host

import java.nio.file.Path

/** The plugin host's files as written for Node: the [folder] holding them, and the [script] Node runs. */
data class HostFiles(val folder: Path, val script: Path)
