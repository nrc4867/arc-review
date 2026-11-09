package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One run in a run list: its state, workflow and trigger, repo and ref, why it is the person's, its jobs, its time
 * and the buttons its state offers. A run waiting on the person is washed in the warning color; one whose page is
 * [open] has an accent edge. Only the optional [columns] given are drawn. A click opens it; a right-click opens [menu].
 */
@Composable
fun RunRow(
    run: WorkflowRun,
    why: WhyMine?,
    jobs: List<RunJob>?,
    login: String?,
    now: Long,
    usual: Long?,
    open: Boolean,
    watched: Boolean,
    waitingOnMe: Boolean,
    hasArtifacts: Boolean,
    controller: RunController,
    onOpen: () -> Unit,
    columns: RunColumns = RunColumns.All,
    menu: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val c = Stretch.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val edge = RunSizes.selectedEdge
    ContextMenuArea(menu) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = RunSizes.row)
                .background(if (waitingOnMe) c.warnWash else if (hovered) c.surfaceControl else Color.Transparent)
                .drawBehind { if (open) drawLine(c.accent, Offset(edge.toPx() / 2, 0f), Offset(edge.toPx() / 2, size.height), edge.toPx()) }
                .bottomRule(c.chartGrid)
                .hoverable(interaction)
                .clickable(interaction, null, role = Role.Button, onClick = onOpen)
                .padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(RunSizes.stateColumn)) { RunStateIcon(run.state) }
            RunTitleCell(run, login, Modifier.weight(1f))
            RunSourceCell(run, Modifier.width(RunSizes.sourceColumn))
            if (columns.why) Box(Modifier.width(RunSizes.whyColumn)) { why?.let { WhyChip(it) } }
            if (columns.jobs) RunJobsCell(run, jobs, Modifier.width(RunSizes.jobsColumn))
            if (columns.time) RunTimeCell(run, now, usual, Modifier.width(RunSizes.timeColumn))
            if (columns.actions) RunRowActions(run, waitingOnMe, watched, hasArtifacts, controller, onOpen, Modifier.width(RunSizes.actionsColumn))
        }
    }
}
