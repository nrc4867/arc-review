package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.CoroutineScope

/** Starts programs as real processes, reading their streams on [scope]. */
class SystemProcessLauncher(private val scope: CoroutineScope) : ProcessLauncher {
    override fun launch(spec: LaunchSpec): LineConnection = ProcessConnection(builder(spec).start(), scope)

    companion object {
        /** The process [spec] describes, ready to start, with the environment [ProcessEnvironment] gives it. */
        fun builder(spec: LaunchSpec): ProcessBuilder {
            val builder = ProcessBuilder(spec.command).directory(spec.workingDirectory.toFile())
            ProcessEnvironment.apply(spec, builder.environment())
            return builder
        }
    }
}
