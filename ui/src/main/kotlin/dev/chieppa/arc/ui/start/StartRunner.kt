package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.start.StartAction
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.ActionId

/** Runs the start screen's clicks through the action registry, each with its arguments as the target. */
object StartRunner {
    /** Whether [action] is registered and can run with its arguments now. */
    fun available(workbench: Workbench, action: StartAction): Boolean {
        val registered = workbench.registry[ActionId(action.id)] ?: return false
        return workbench.withTarget(MenuTarget.OfStart(action)) { registered.isAvailable() }
    }

    /** Runs [action] when it can run now. */
    fun run(workbench: Workbench, action: StartAction) {
        val registered = workbench.registry[ActionId(action.id)] ?: return
        workbench.withTarget(MenuTarget.OfStart(action)) {
            if (registered.isAvailable()) registered.run()
        }
    }
}
