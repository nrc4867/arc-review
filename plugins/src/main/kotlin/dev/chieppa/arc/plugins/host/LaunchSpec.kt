package dev.chieppa.arc.plugins.host

import java.nio.file.Path

/** A process to start: the program and its arguments in [command], run in [workingDirectory] with [environment] added. */
data class LaunchSpec(val command: List<String>, val workingDirectory: Path, val environment: Map<String, String> = emptyMap())
