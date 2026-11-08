package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartButton
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.core.start.StartColumn
import dev.chieppa.arc.core.start.StartContribution
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_click
import dev.chieppa.arc.design.generated.start_key_select
import dev.chieppa.arc.design.generated.start_keys_several
import dev.chieppa.arc.design.generated.start_progress
import dev.chieppa.arc.design.generated.start_waiting
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.KeyChord
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.caps
import org.jetbrains.compose.resources.stringResource

private val SCREEN = setOf(StartScreenKind.SeveralSources)

private val KEYS = listOf(ArcActionIds.NEXT_FILE, ArcActionIds.NEXT_CHANGE, ArcActionIds.CLOSE_PANE)

/** The chord whose modifier adds a source to the selection with a click: Ctrl, Cmd on macOS. */
private val SELECT_CHORD = KeyChord.parse("Ctrl A")

/**
 * Arc's cards and buttons for several selected [sources]: Open side by side; Progress, one row per source; then Waiting
 * on you, grouped by source, and Keys with several sources beside it.
 */
@Composable
fun severalSourcesCards(workbench: Workbench, sources: List<Source>): StartContribution {
    val sideBySide = SideBySide.openable(workbench, sources).size > 1
    val select = StartRow(stringResource(Res.string.start_key_select), trailing = StartTrailing.Caps(SELECT_CHORD.caps().dropLast(1) + stringResource(Res.string.start_click)))
    return StartContribution(
        cards = listOf(
            StartCard(StartCards.PROGRESS, stringResource(Res.string.start_progress), SCREEN, StartColumn.Main, StartIcons.CHECK_RUN),
            StartCard(StartCards.WAITING, stringResource(Res.string.start_waiting), SCREEN, StartColumn.Side, StartIcons.EYE, rows = waitingBySource(workbench, sources)),
            StartCard(StartCards.KEYS, stringResource(Res.string.start_keys_several), SCREEN, StartColumn.Side, StartIcons.KEYBOARD, rows = listOf(select) + keyRows(workbench, KEYS)),
        ),
        buttons = listOfNotNull(
            if (!sideBySide) null else StartButton(
                LocalActionText.current(ArcActionIds.OPEN_SIDE_BY_SIDE.value),
                ArcStartActions.of(ArcActionIds.OPEN_SIDE_BY_SIDE), StartIcons.SPLIT_VIEW, primary = true,
            ),
        ),
    )
}

/** What waits on the person in each of [sources], under a row naming the source and how many wait there. */
@Composable
private fun waitingBySource(workbench: Workbench, sources: List<Source>): List<StartRow> = sources.flatMap { source ->
    val rows = waitingRows(workbench, source)
    if (rows.isEmpty()) emptyList() else listOf(StartRow(SourceLabels.of(source), trailing = StartTrailing.Count(rows.size))) + rows
}
