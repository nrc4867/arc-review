package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.artifacts.JUnitReport
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunFile
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Link

/** A JUnit report's tests, failures first and washed, each failure's `path:line` opening the file at the run's commit. */
@Composable
fun JUnitTable(report: JUnitReport, run: WorkflowRun, workbench: Workbench) {
    val c = Stretch.colors
    Column {
        for (case in report.cases.take(MAX_CASES)) {
            Row(
                Modifier.fillMaxWidth().background(if (case.failed) c.errorWash else Color.Transparent).bottomRule(c.chartGrid).padding(vertical = StretchSpacing.space6, horizontal = StretchSpacing.space4),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RunStateIcon(if (case.failed) RunState.Failed else if (case.skipped) RunState.Skipped else RunState.Succeeded)
                Column(Modifier.weight(1f)) {
                    RunText(case.name, RunTextKind.Body, c.ink)
                    case.location?.let { place ->
                        val (path, line) = place.substringBeforeLast(':') to place.substringAfterLast(':').toIntOrNull()
                        Link(place, { workbench.openRunFile(run, path, line) })
                    }
                }
                RunText(case.seconds?.let { String.format(java.util.Locale.ROOT, "%.2fs", it) }.orEmpty(), RunTextKind.MonoSmall, c.inkSubtle)
            }
        }
    }
}

private const val MAX_CASES = 500
