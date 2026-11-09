package dev.chieppa.arc.ui.actions

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_approve_fork_run
import dev.chieppa.arc.design.generated.action_cancel_run
import dev.chieppa.arc.design.generated.action_copy_run_link
import dev.chieppa.arc.design.generated.action_open_run
import dev.chieppa.arc.design.generated.action_open_run_commit
import dev.chieppa.arc.design.generated.action_open_run_on_github
import dev.chieppa.arc.design.generated.action_open_runs
import dev.chieppa.arc.design.generated.action_rerun_failed
import dev.chieppa.arc.design.generated.action_rerun_run
import dev.chieppa.arc.design.generated.action_run_workflow
import dev.chieppa.arc.design.generated.action_run_workflow_again
import dev.chieppa.arc.design.generated.action_watch_run
import dev.chieppa.arc.design.generated.action_watch_workflow
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.StringResource

/** The run actions' texts and icons, by their ids. */
object RunActionTexts {
    val texts: Map<String, StringResource> = mapOf(
        ArcActionIds.OPEN_RUNS.value to Res.string.action_open_runs,
        ArcActionIds.RUN_WORKFLOW.value to Res.string.action_run_workflow,
        ArcActionIds.OPEN_RUN.value to Res.string.action_open_run,
        ArcActionIds.OPEN_RUN_COMMIT.value to Res.string.action_open_run_commit,
        ArcActionIds.WATCH_RUN.value to Res.string.action_watch_run,
        ArcActionIds.WATCH_WORKFLOW.value to Res.string.action_watch_workflow,
        ArcActionIds.RERUN_RUN.value to Res.string.action_rerun_run,
        ArcActionIds.RERUN_FAILED.value to Res.string.action_rerun_failed,
        ArcActionIds.CANCEL_RUN.value to Res.string.action_cancel_run,
        ArcActionIds.APPROVE_FORK_RUN.value to Res.string.action_approve_fork_run,
        ArcActionIds.RUN_WORKFLOW_AGAIN.value to Res.string.action_run_workflow_again,
        ArcActionIds.OPEN_RUN_ON_GITHUB.value to Res.string.action_open_run_on_github,
        ArcActionIds.COPY_RUN_LINK.value to Res.string.action_copy_run_link,
    )

    val icons: Map<String, ImageVector> = mapOf(
        ArcActionIds.OPEN_RUNS.value to ArcIcons.Runs,
        ArcActionIds.RUN_WORKFLOW.value to ArcIcons.RunWorkflow,
        ArcActionIds.OPEN_RUN.value to ArcIcons.TearOut,
        ArcActionIds.OPEN_RUN_COMMIT.value to StretchIcons.Layers,
        ArcActionIds.WATCH_RUN.value to StretchIcons.Eye,
        ArcActionIds.WATCH_WORKFLOW.value to StretchIcons.Eye,
        ArcActionIds.RERUN_RUN.value to ArcIcons.Refresh,
        ArcActionIds.RERUN_FAILED.value to ArcIcons.Refresh,
        ArcActionIds.CANCEL_RUN.value to ArcIcons.Stop,
        ArcActionIds.APPROVE_FORK_RUN.value to ArcIcons.Gate,
        ArcActionIds.RUN_WORKFLOW_AGAIN.value to ArcIcons.RunWorkflow,
        ArcActionIds.OPEN_RUN_ON_GITHUB.value to StretchIcons.ExternalLink,
        ArcActionIds.COPY_RUN_LINK.value to StretchIcons.Link,
    )
}
