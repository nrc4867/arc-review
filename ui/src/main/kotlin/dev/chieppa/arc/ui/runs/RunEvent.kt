package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.RunStall

/** Something about a run a notification that is on was waiting for, under [kind]. */
data class RunEvent(val kind: RunNotify, val run: WorkflowRun, val stall: RunStall? = null)
