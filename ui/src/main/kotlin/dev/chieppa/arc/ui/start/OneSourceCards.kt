package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartButton
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartContribution
import dev.chieppa.arc.core.start.StartLink
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartTone
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.design.generated.start_changed_since
import dev.chieppa.arc.design.generated.start_compare
import dev.chieppa.arc.design.generated.start_continue
import dev.chieppa.arc.design.generated.start_conversation
import dev.chieppa.arc.design.generated.start_keys_reviewing
import dev.chieppa.arc.design.generated.start_notes
import dev.chieppa.arc.design.generated.start_open_on_github
import dev.chieppa.arc.design.generated.start_remove
import dev.chieppa.arc.design.generated.start_revision
import dev.chieppa.arc.design.generated.start_revision_commits
import dev.chieppa.arc.design.generated.start_reviewed
import dev.chieppa.arc.design.generated.start_revisions
import dev.chieppa.arc.design.generated.start_this_source
import dev.chieppa.arc.design.generated.start_waiting
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val SCREEN = setOf(StartScreenKind.OneSource)

private val KEYS = listOf(
    ArcActionIds.NEXT_FILE,
    ArcActionIds.NEXT_CHANGE,
    ArcActionIds.TOGGLE_REVIEWED,
    ArcActionIds.COMMENT_HERE,
    ArcActionIds.FIND_IN_FILE,
    ArcActionIds.TOGGLE_WRAP,
)

/**
 * Arc's cards and buttons for one selected [source]: Continue, Changed since you reviewed and Conversation; Waiting on
 * you and Your notes, then Revisions, Keys while reviewing and This source beside them.
 */
@Composable
fun oneSourceCards(workbench: Workbench, source: Source): StartContribution {
    val state = workbench.state
    val next = StartReview.nextFile(state, source)
    val changed = StartReview.count(state, source, FileReviewState.ChangedSinceReviewed)
    val pull = state.pulls[source]
    val buttons = listOfNotNull(
        next?.let {
            StartButton(
                stringResource(Res.string.start_continue, StartReview.name(it)), ArcStartActions.on(ArcActionIds.CONTINUE_REVIEW, source),
                StartIcons.CONTINUE, keys = ArcActionIds.NEXT_FILE.value, primary = true,
            )
        },
        if (changed == 0) null else StartButton(stringResource(Res.string.start_changed_since, changed), ArcStartActions.of(ArcActionIds.TOGGLE_SINCE_REVIEW), StartIcons.CHANGED_SINCE),
        if (source !is Source.PullRequest) null else StartButton(stringResource(Res.string.start_conversation), ArcStartActions.on(ArcActionIds.OPEN_CONVERSATION, source), StartIcons.COMMENT),
    )
    val thisSource = listOfNotNull(
        pull?.let { StartRow(stringResource(Res.string.start_open_on_github), StartIcons.EXTERNAL_LINK, action = ArcStartActions.openLink(it.url)) },
        StartRow(stringResource(Res.string.start_remove), StartIcons.CLOSE, action = ArcStartActions.on(ArcActionIds.REMOVE_SOURCE, source)),
    )
    return StartContribution(
        cards = listOf(
            StartCard(StartCards.WAITING, stringResource(Res.string.start_waiting), SCREEN, StartColumn.Main, StartIcons.EYE, rows = waitingRows(workbench, source)),
            StartCard(StartCards.NOTES, stringResource(Res.string.start_notes), SCREEN, StartColumn.Main, StartIcons.NOTE, rows = noteRows(workbench, source)),
            StartCard(
                StartCards.REVISIONS, stringResource(Res.string.start_revisions), SCREEN, StartColumn.Side, StartIcons.MATRIX,
                rows = revisionRows(workbench, source),
                header = StartLink(stringResource(Res.string.start_compare), ArcStartActions.on(ArcActionIds.OPEN_REVISIONS, source)),
            ),
            StartCard(StartCards.KEYS, stringResource(Res.string.start_keys_reviewing), SCREEN, StartColumn.Side, StartIcons.KEYBOARD, rows = keyRows(workbench, KEYS)),
            StartCard(StartCards.THIS_SOURCE, stringResource(Res.string.start_this_source), SCREEN, StartColumn.Side, StartIcons.of(source), rows = thisSource),
        ),
        buttons = buttons,
    )
}

/** The person's notes on [source], each opening the file it is on, or the Notes drawer for a note on the whole source. */
@Composable
private fun noteRows(workbench: Workbench, source: Source): List<StartRow> =
    workbench.comments.controller.local.of(source).filter { it.isNote && it.body.isNotBlank() }.map { note ->
        val anchor = note.anchor
        StartRow(
            title = note.body.lineSequence().first { it.isNotBlank() }.trim(),
            icon = StartIcons.NOTE,
            subtitle = anchor?.let { stringResource(Res.string.staged_location, it.path, it.line) },
            action = anchor?.let { ArcStartActions.openChange(source, it.path) } ?: ArcStartActions.of(ArcActionIds.toggleDrawer(DrawerIds.NOTES)),
        )
    }

/** [source]'s revisions, newest first: the one the person reviewed marked, the others with when they came. */
@Composable
private fun revisionRows(workbench: Workbench, source: Source): List<StartRow> {
    val history = workbench.state.revisions[source]?.history ?: return emptyList()
    val reviewed = workbench.state.workspace.marksIn(source).values.mapNotNull { it.reviewed?.head }.toSet()
    return history.revisions.reversed().map { revision ->
        StartRow(
            title = stringResource(Res.string.start_revision, revision.number),
            subtitle = pluralStringResource(Res.plurals.start_revision_commits, revision.commits.size, revision.head.short(),revision.commits.size),
            trailing = if (revision.head in reviewed) StartTrailing.Chip(stringResource(Res.string.start_reviewed), StartTone.Accent) else StartTrailing.Time(revision.at),
            action = ArcStartActions.on(ArcActionIds.OPEN_REVISIONS, source),
        )
    }
}
