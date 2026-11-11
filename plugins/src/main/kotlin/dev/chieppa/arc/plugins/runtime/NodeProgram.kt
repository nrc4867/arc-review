package dev.chieppa.arc.plugins.runtime

import java.nio.file.Path

/** A Node.js program and its [major] version. */
data class NodeProgram(val path: Path, val major: Int)
