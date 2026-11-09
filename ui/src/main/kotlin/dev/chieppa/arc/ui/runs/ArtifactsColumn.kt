package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.artifacts.ArtifactJobs
import dev.chieppa.arc.core.actions.artifacts.ArtifactKind
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.artifact_file_kind
import dev.chieppa.arc.design.generated.run_artifacts_count
import dev.chieppa.arc.design.generated.run_cache
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openArtifactPage
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.document.rememberFileChooser
import dev.chieppa.stretch.theme.Stretch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.nio.file.Files

/**
 * A finished run's artifacts: each with the job that made it, when it expires and its size, and a button to save it
 * where the person picks. A click previews it below: text, Markdown, JSON or a picture, a JUnit report as a table of
 * its tests, and HTML opened in the browser. The preview can be compared with the same artifact of an earlier run.
 */
@Composable
fun ArtifactsColumn(run: WorkflowRun, detail: RunDetail?, state: RunPageState, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val artifacts = detail?.artifacts.orEmpty()
    val jobs = remember(detail) { ArtifactJobs.of(artifacts.map { it.name }, detail?.jobs.orEmpty(), detail?.logs.orEmpty()) }
    val chooser = rememberFileChooser()
    val scope = rememberCoroutineScope()
    val kind = stringResource(Res.string.artifact_file_kind)
    var content by remember(run.ref, state.artifact) { mutableStateOf<ArtifactContent?>(null) }
    val picked = artifacts.firstOrNull { it.id == state.artifact }
    LaunchedEffect(run.ref, picked?.id) {
        content = picked?.let { ArtifactReader.read(workbench.actions, run.ref, it, workbench.dispatchers.io) }
        val html = content
        if (html != null && html.kind == ArtifactKind.Html) workbench.openArtifactPage(run, html)
    }
    fun save(artifact: RunArtifact) {
        scope.launch {
            val file = ArtifactReader.read(workbench.actions, run.ref, artifact, workbench.dispatchers.io)?.file ?: workbench.actions.artifactFile(run.ref, artifact) ?: return@launch
            val zipped = withContext(workbench.dispatchers.io) { Files.newInputStream(file).use { it.readNBytes(2) }.contentEquals(byteArrayOf(0x50, 0x4B)) }
            val target = chooser.chooseSave(ArtifactFormat(kind, if (zipped) ZIP else artifact.name.substringAfterLast('.', BIN)), artifact.name) ?: return@launch
            withContext(workbench.dispatchers.io) { Files.copy(file, File(target).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING) }
        }
    }
    Column(modifier.verticalScroll(rememberScrollState())) {
        PaneHead(ArcIcons.Artifact, stringResource(Res.string.run_artifacts_count, artifacts.size), stringResource(Res.string.run_cache, Sizes.bytes(workbench.actions.cacheBytes()), Sizes.bytes(workbench.actions.cacheCap)))
        for (artifact in artifacts) {
            ArtifactRow(artifact, jobs[artifact.name], artifact.id == state.artifact, { state.artifact = artifact.id; state.comparing = false }, { save(artifact) })
        }
        val shown = content
        if (picked != null && shown != null && shown.kind != ArtifactKind.Html) {
            ArtifactPreview(run, picked, shown, state, workbench, Modifier.fillMaxWidth())
        }
    }
}

private const val ZIP = "zip"
private const val BIN = "bin"
