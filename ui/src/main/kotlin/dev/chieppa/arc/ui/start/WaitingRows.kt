package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartTone
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_check_failed
import dev.chieppa.arc.design.generated.start_checks_failed
import dev.chieppa.arc.design.generated.start_failed
import dev.chieppa.arc.design.generated.start_replied
import dev.chieppa.arc.design.generated.start_staged
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * What in [source] waits on the person: each open thread they took part in whose latest message is someone else's, the
 * comments they staged and have not pushed, and failed checks.
 */
@Composable
fun waitingRows(workbench: Workbench, source: Source): List<StartRow> {
    val controller = workbench.comments.controller
    val viewer = controller.viewer(source) ?: workbench.watch.viewer?.login
    val replied = controller.hosted[source]?.conversation?.threads.orEmpty().filter { thread ->
        thread.state in OPEN && viewer != null && thread.messages.any { it.author == viewer } && thread.messages.lastOrNull()?.author != viewer
    }
    val replies = replied.map { thread ->
        val last = thread.messages.last()
        StartRow(
            title = stringResource(Res.string.start_replied, last.author, thread.line),
            icon = StartIcons.REPLY,
            subtitle = stringResource(Res.string.staged_location, thread.path, thread.line),
            trailing = StartTrailing.Time(last.writtenAt),
            action = ArcStartActions.openChange(source, thread.path),
        )
    }
    val staged = controller.local.staged(source).size
    val stagedRow = if (staged == 0 || source != workbench.state.active) null else StartRow(
        title = pluralStringResource(Res.plurals.start_staged, staged, staged),
        icon = StartIcons.STAGE,
        trailing = StartTrailing.Keys(ArcActionIds.PUSH_REVIEW.value),
        action = ArcStartActions.of(ArcActionIds.REVIEW_STAGED),
    )
    val pull = workbench.state.pulls[source]
    val checksRow = pull?.takeIf { it.checks.state == CheckState.Failed }?.let {
        StartRow(
            title = it.checks.failed.firstOrNull()?.let { check -> stringResource(Res.string.start_check_failed, check) } ?: stringResource(Res.string.start_checks_failed),
            icon = StartIcons.CHECK_FAIL,
            trailing = StartTrailing.Chip(stringResource(Res.string.start_failed), StartTone.Error),
            action = ArcStartActions.openLink(ArcLinks.checks(it.url)),
        )
    }
    return replies + listOfNotNull(stagedRow, checksRow)
}

private val OPEN = setOf(ThreadState.Open, ThreadState.Addressed)
