package dev.chieppa.arc.plugins.host

import java.nio.file.Path

/**
 * A process to start: the program and its arguments in [command], run in [workingDirectory]. With [environment] it
 * gets exactly that environment; without, Arc's own less [ProcessEnvironment.SECRETS].
 */
data class LaunchSpec(val command: List<String>, val workingDirectory: Path, val environment: Map<String, String>? = null)
