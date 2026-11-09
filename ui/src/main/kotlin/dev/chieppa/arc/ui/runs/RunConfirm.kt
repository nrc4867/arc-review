package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.actions.ForkChanges

/** A write to GitHub Actions waiting for the person to confirm it, by kind. */
sealed interface RunConfirm {
    val run: WorkflowRun

    /** Re-run every job, or only the failed ones. */
    data class Rerun(override val run: WorkflowRun, val failedOnly: Boolean) : RunConfirm

    /** Re-run one finished job. */
    data class RerunJob(override val run: WorkflowRun, val job: RunJob) : RunConfirm

    /** Cancel the run, and when [job] is given, re-run that job once it has stopped. */
    data class Cancel(override val run: WorkflowRun, val job: RunJob? = null) : RunConfirm

    /** Let a fork's run go ahead; [changes] is what its pull request changes under `.github/`, once read. */
    data class ApproveFork(override val run: WorkflowRun, val changes: ForkChanges? = null, val reading: Boolean = true) : RunConfirm
}
