package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.log.KeptLog
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_full_log
import dev.chieppa.arc.design.generated.run_kept_tail
import dev.chieppa.arc.design.generated.run_log_folded
import dev.chieppa.arc.design.generated.run_log_silent
import dev.chieppa.arc.design.generated.run_step_finished
import dev.chieppa.stretch.console.Console
import dev.chieppa.stretch.console.ConsoleState
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunFile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * The kept log of the picked job, or of its picked step, in the console, where `path:line` opens the file at the run's
 * commit. Full log reads the whole log from GitHub into the console without keeping it. A running job has no log yet.
 */
@Composable
fun LogColumn(run: WorkflowRun, job: RunJob?, log: KeptLog?, state: RunPageState, workbench: Workbench, modifier: Modifier = Modifier) {
    val console = remember(run.ref, job?.id) { ConsoleState() }
    var full by remember(run.ref, job?.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val step = job?.steps?.firstOrNull { it.number == state.step }
    val folded = stringResource(Res.string.run_log_folded)
    val silent = stringResource(Res.string.run_log_silent)
    LaunchedEffect(log, state.step, full) {
        if (full) return@LaunchedEffect
        console.clear()
        if (log == null) return@LaunchedEffect
        val lines = step?.let { log.step(it.number) } ?: log.lines
        console.append(LogLines.text(lines, log.silences, { folded.format(it) }, { silent.format(it) }))
    }
    Column(modifier) {
        val title = step?.name ?: job?.name.orEmpty()
        val detail = when {
            step != null && step.completedAt != null -> stringResource(Res.string.run_step_finished, RunDurations.of(step.seconds(0) ?: 0))
            log != null -> stringResource(Res.string.run_kept_tail, Sizes.bytes(log.keptBytes), Sizes.bytes(log.totalBytes))
            else -> ""
        }
        PaneHead(StretchIcons.Command, title, detail) {
            if (job != null && job.state.finished) {
                Button(stringResource(Res.string.run_full_log), {
                    full = true
                    scope.launch { FullLog.into(console, workbench.actions, run, job.id, workbench.dispatchers.io) }
                }, kind = ButtonKind.Quiet, icon = StretchIcons.Command)
            }
        }
        Console(console, Modifier.weight(1f).fillMaxWidth(), onLinkClick = { link -> workbench.openRunFile(run, link.target, link.line) })
    }
}
