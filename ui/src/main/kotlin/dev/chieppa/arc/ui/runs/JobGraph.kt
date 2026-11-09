package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.timing.RunStall
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** A run's jobs in [columns], left to right by what they wait for, joined by links; a click on a job picks it. */
@Composable
fun JobGraph(columns: List<List<RunJob>>, selected: Long?, stalls: List<RunStall>, now: Long, onPick: (RunJob) -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().background(c.surfaceCanvas).horizontalScroll(rememberScrollState()).padding(RunSizes.graphPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { index, column ->
            if (index > 0) Box(Modifier.width(RunSizes.link).height(RunSizes.linkStroke).background(c.lineStrong))
            Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
                for (job in column) JobNode(job, job.id == selected, stalls.any { it.jobId == job.id }, now) { onPick(job) }
            }
        }
    }
}
