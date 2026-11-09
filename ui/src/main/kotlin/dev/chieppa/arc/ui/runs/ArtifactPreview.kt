package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.artifacts.ArtifactKind
import dev.chieppa.arc.core.actions.artifacts.JUnitReport
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.artifact_compare
import dev.chieppa.arc.design.generated.artifact_compare_close
import dev.chieppa.arc.design.generated.artifact_entry
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.diff.DiffViewer
import dev.chieppa.stretch.diff.diffTexts
import dev.chieppa.stretch.markdown.MarkdownView
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.ImageView
import dev.chieppa.stretch.ui.decodeImage
import org.jetbrains.compose.resources.stringResource

/**
 * The picked artifact shown under the list: a JUnit report as its tests, failures first; Markdown, JSON, text or a
 * picture as they are. When an earlier run of the same workflow uploaded an artifact of the same name, Compare shows
 * the two in the diff viewer.
 */
@Composable
fun ArtifactPreview(run: WorkflowRun, artifact: RunArtifact, content: ArtifactContent, state: RunPageState, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val earlier = workbench.runs.watch.all().filter { it.repo.equals(run.repo, true) && it.workflowPath == run.workflowPath && it.id < run.id && it.state.finished }.maxByOrNull { it.id }
    var other by remember(run.ref, artifact.id, earlier?.ref) { mutableStateOf<ArtifactContent?>(null) }
    LaunchedEffect(state.comparing, earlier?.ref) {
        if (!state.comparing || earlier == null) return@LaunchedEffect
        val earlierArtifact = workbench.runs.earlierArtifact(earlier, artifact.name) ?: return@LaunchedEffect
        other = ArtifactReader.read(workbench.actions, earlier.ref, earlierArtifact, workbench.dispatchers.io, content.entry)
    }
    Column(modifier.padding(StretchSpacing.space10), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RunText(stringResource(Res.string.artifact_entry, artifact.name, content.entry.substringAfterLast('/')), RunTextKind.Strong, c.inkStrong, Modifier.weight(1f))
            if (earlier != null) {
                if (state.comparing) {
                    Button(stringResource(Res.string.artifact_compare_close), { state.comparing = false }, kind = ButtonKind.Quiet)
                } else {
                    Button(stringResource(Res.string.artifact_compare, earlier.number), { state.comparing = true }, kind = ButtonKind.Quiet, icon = ArcIcons.Compare)
                }
            }
        }
        val before = other
        when {
            state.comparing && before != null ->
                DiffViewer(listOf(diffTexts(before.text(), content.text(), content.entry)), Modifier.fillMaxWidth().heightIn(min = RunSizes.previewHeight, max = RunSizes.previewHeight * 2))
            content.kind == ArtifactKind.JUnit -> JUnitReport.parse(content.bytes)?.let { JUnitTable(it, run, workbench) }
            content.kind == ArtifactKind.Markdown -> MarkdownView(content.text(), onLinkClick = { workbench.links.open(it) })
            content.kind == ArtifactKind.Image -> decodeImage(content.bytes)?.let { ImageView(it, Modifier.fillMaxWidth(), content.entry) }
            else -> BasicText(content.text().lineSequence().take(MAX_LINES).joinToString("\n"), style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)))
        }
    }
}

private const val MAX_LINES = 2000
