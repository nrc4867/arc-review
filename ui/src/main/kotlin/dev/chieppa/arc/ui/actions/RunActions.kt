package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.runs.RunConfirm
import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunCommit
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry

/**
 * Registers the actions on workflow runs. Opening the person's runs and starting a workflow run from anywhere; the
 * others act on the run a menu is open on, or the run whose page is focused. Every write asks to be confirmed first.
 */
fun registerRunActions(registry: ActionRegistry, workbench: Workbench) {
    val controller = workbench.runs
    fun action(id: ActionId, available: (WorkflowRun) -> Boolean, run: (WorkflowRun) -> Unit) =
        registry.register(
            Action(
                id,
                id.value,
                id.value,
                emptyList(),
                isAvailable = { ActionTargets.run(workbench)?.let(available) == true },
                run = { ActionTargets.run(workbench)?.let(run) },
            ),
        )

    registry.register(Action(ArcActionIds.OPEN_RUNS, ArcActionIds.OPEN_RUNS.value, ArcActionIds.OPEN_RUNS.value, emptyList(), run = { workbench.panes.openRuns(RunWatchlist.MINE) }))
    registry.register(
        Action(ArcActionIds.RUN_WORKFLOW, ArcActionIds.RUN_WORKFLOW.value, ArcActionIds.RUN_WORKFLOW.value, emptyList(), run = {
            controller.startDispatch(ActionTargets.run(workbench)?.let { GitHubRepo.parse(it.repo) })
        }),
    )
    action(ArcActionIds.OPEN_RUN, { true }) { workbench.panes.openRun(it.ref) }
    action(ArcActionIds.OPEN_RUN_COMMIT, { true }) { workbench.openRunCommit(it) }
    action(ArcActionIds.WATCH_RUN, { true }) { controller.toggleWatch(it) }
    action(ArcActionIds.WATCH_WORKFLOW, { true }) { run -> controller.watch.watchWorkflow(run, !controller.watch.watchlist.workflows.any { it.repo.equals(run.repo, true) && it.path == run.workflowPath }) }
    action(ArcActionIds.RERUN_RUN, { it.state.finished }) { controller.ask(RunConfirm.Rerun(it, failedOnly = false)) }
    action(ArcActionIds.RERUN_FAILED, { it.state == RunState.Failed }) { controller.ask(RunConfirm.Rerun(it, failedOnly = true)) }
    action(ArcActionIds.CANCEL_RUN, { !it.state.finished && !it.awaitsForkApproval }) { controller.ask(RunConfirm.Cancel(it)) }
    action(ArcActionIds.APPROVE_FORK_RUN, { it.awaitsForkApproval && it.ref in controller.watch.waitingOnMe }) { controller.ask(RunConfirm.ApproveFork(it)) }
    action(ArcActionIds.RUN_WORKFLOW_AGAIN, { it.event == WORKFLOW_DISPATCH }) { controller.startDispatch(GitHubRepo.parse(it.repo), it.workflowPath) }
    action(ArcActionIds.OPEN_RUN_ON_GITHUB, { it.htmlUrl.isNotEmpty() }) { workbench.links.open(it.htmlUrl) }
    action(ArcActionIds.COPY_RUN_LINK, { it.htmlUrl.isNotEmpty() }) { workbench.clipboard.copy(it.htmlUrl) }
}

private const val WORKFLOW_DISPATCH = "workflow_dispatch"
