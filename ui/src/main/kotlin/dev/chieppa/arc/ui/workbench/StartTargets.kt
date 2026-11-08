package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartAction
import dev.chieppa.arc.core.start.StartArgs

/** What a start screen action's arguments name in the workspace. */
object StartTargets {
    /** The source in the strip whose ref the action names, if it names one there. */
    fun source(workbench: Workbench, action: StartAction): Source? =
        action[StartArgs.SOURCE]?.let { ref -> workbench.state.workspace.sources.firstOrNull { it.ref == ref } }

    /** The changed file the action names: its source in the strip and its path. */
    fun file(workbench: Workbench, action: StartAction): Pair<Source, String>? {
        val path = action[StartArgs.PATH] ?: return null
        return source(workbench, action)?.let { it to path }
    }
}
