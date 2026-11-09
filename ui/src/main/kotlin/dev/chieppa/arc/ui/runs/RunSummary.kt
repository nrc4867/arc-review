package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.TimingKey
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_annotations
import dev.chieppa.arc.design.generated.run_col_job
import dev.chieppa.arc.design.generated.run_col_took
import dev.chieppa.arc.design.generated.run_full_log
import dev.chieppa.arc.design.generated.run_jobs
import dev.chieppa.arc.design.generated.run_log_folded
import dev.chieppa.arc.design.generated.run_log_silent
import dev.chieppa.arc.design.generated.run_step_usual
import dev.chieppa.arc.design.generated.run_summary_artifact
import dev.chieppa.arc.design.generated.run_summary_on_github
import dev.chieppa.arc.design.generated.run_tail_of
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunFile
import dev.chieppa.stretch.console.Console
import dev.chieppa.stretch.console.ConsoleState
import dev.chieppa.stretch.markdown.MarkdownView
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Link
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A finished run's summary, built from what GitHub's API gives: each job's result and time against its usual one,
 * the annotations, each failed job's kept tail, and the workflow's own Markdown summary when the run uploaded it as
 * the repo's summary artifact, else a link to the summary on GitHub.
 */
@Composable
fun RunSummary(run: WorkflowRun, detail: RunDetail?, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val watch = workbench.runs.watch
    val jobs = detail?.jobs.orEmpty()
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = StretchSpacing.space16, vertical = StretchSpacing.space14), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space16)) {
        Column {
            RunText(stringResource(Res.string.run_jobs), RunTextKind.Eyebrow, c.inkMuted)
            Row(Modifier.fillMaxWidth().bottomRule(c.linePanel).padding(vertical = StretchSpacing.space6), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
                RunText("", RunTextKind.Eyebrow, c.inkMuted, Modifier.width(ArcSizes.pullIcon))
                RunText(stringResource(Res.string.run_col_job), RunTextKind.Eyebrow, c.inkMuted, Modifier.weight(1f))
                RunText(stringResource(Res.string.run_col_took), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.usualColumn), TextAlign.End)
                RunText(stringResource(Res.string.run_step_usual), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.usualColumn), TextAlign.End)
            }
            for (job in jobs) {
                val failed = job.state == RunState.Failed
                Row(
                    Modifier.fillMaxWidth().background(if (failed) c.errorWash else androidx.compose.ui.graphics.Color.Transparent).bottomRule(c.chartGrid).padding(vertical = StretchSpacing.space6),
                    horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RunStateIcon(job.state, Modifier.width(ArcSizes.pullIcon))
                    RunText(job.name, RunTextKind.Strong, c.inkStrong, Modifier.weight(1f))
                    RunText(job.seconds(0)?.let(RunDurations::of).orEmpty(), RunTextKind.MonoSmall, c.ink, Modifier.width(RunSizes.usualColumn), TextAlign.End)
                    val usual = watch.timings.jobP90(TimingKey(run.repo, run.workflowPath, job.name))
                    RunText(usual?.let(RunDurations::of).orEmpty(), RunTextKind.MonoSmall, c.inkSubtle, Modifier.width(RunSizes.usualColumn), TextAlign.End)
                }
            }
        }
        val annotations = detail?.annotations.orEmpty()
        if (annotations.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
                val errors = annotations.count { it.error }
                RunText(pluralStringResource(Res.plurals.run_annotations, annotations.size, errors, annotations.size - errors), RunTextKind.Eyebrow, c.inkMuted)
                for (annotation in annotations) {
                    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (annotation.error) ArcIcons.CheckFail else ArcIcons.Warning, if (annotation.error) c.errorInk else c.warnInk, size = ArcSizes.checkMark)
                        val place = "${annotation.path.substringAfterLast('/')}:${annotation.line}"
                        Link(place, { workbench.openRunFile(run, annotation.path, annotation.line) })
                        RunText(annotation.message.lineSequence().first(), RunTextKind.Small, c.ink, Modifier.weight(1f))
                        RunText(annotation.job, RunTextKind.Small, c.inkSubtle)
                    }
                }
            }
        }
        for (job in jobs.filter { it.state == RunState.Failed }) {
            val log = detail?.logs?.get(job.id) ?: continue
            val step = job.steps.firstOrNull { it.state == RunState.Failed }
            val console = remember(run.ref, job.id) { ConsoleState() }
            val folded = stringResource(Res.string.run_log_folded)
            val silent = stringResource(Res.string.run_log_silent)
            LaunchedEffect(log) {
                console.clear()
                console.append(LogLines.text(step?.let { log.step(it.number) } ?: log.lines, log.silences, { folded.format(it) }, { silent.format(it) }))
            }
            val scope = rememberCoroutineScope()
            Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RunText(stringResource(Res.string.run_tail_of, job.name, step?.name.orEmpty()), RunTextKind.Eyebrow, c.inkMuted, Modifier.weight(1f))
                    Button(stringResource(Res.string.run_full_log), { scope.launch { FullLog.into(console, workbench.actions, run, job.id, workbench.dispatchers.io) } }, kind = ButtonKind.Quiet, icon = StretchIcons.Command)
                }
                val shape = Stretch.shapes.ctlShape
                Console(console, Modifier.fillMaxWidth().height(RunSizes.consoleMin).border(Stretch.stroke, c.linePanel, shape), onLinkClick = { workbench.openRunFile(run, it.target, it.line) })
            }
        }
        WorkflowSummary(run, detail, workbench)
    }
}

@Composable
private fun WorkflowSummary(run: WorkflowRun, detail: RunDetail?, workbench: Workbench) {
    val c = Stretch.colors
    val name = workbench.runs.watch.watchlist.summaryArtifact(run.repo)
    val artifact = detail?.artifacts?.firstOrNull { it.name == name }
    var text by remember(run.ref, artifact?.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(run.ref, artifact?.id) {
        if (artifact == null) return@LaunchedEffect
        val content = ArtifactReader.read(workbench.actions, run.ref, artifact, workbench.dispatchers.io)
        text = content?.takeIf { it.kind == dev.chieppa.arc.core.actions.artifacts.ArtifactKind.Markdown }?.text()
    }
    Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
        val shown = text
        if (shown != null) {
            RunText(stringResource(Res.string.run_summary_artifact, name), RunTextKind.Eyebrow, c.inkMuted)
            MarkdownView(shown, onLinkClick = { workbench.links.open(it) })
        } else if (run.htmlUrl.isNotEmpty()) {
            Link(stringResource(Res.string.run_summary_on_github), { workbench.links.open(run.htmlUrl) })
        }
    }
}
