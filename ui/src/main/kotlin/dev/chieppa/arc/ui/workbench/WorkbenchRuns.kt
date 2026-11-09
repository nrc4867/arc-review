package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.runs.ArtifactContent
import dev.chieppa.arc.ui.runs.ArtifactReader
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files

/** Opens [run]'s commit as a source in the strip, cloning its repo first when there is no clone of it here. */
fun Workbench.openRunCommit(run: WorkflowRun) {
    val repo = GitHubRepo.parse(run.repo) ?: return
    val sha = CommitSha.parse(run.headSha) ?: return
    followGitHub(repo) { name -> addSource(Source.Commit(name, sha).ref) }
}

/** Opens the commits from [base] to [head] of [run]'s repo as a source, as a deployment ships them. */
fun Workbench.openRunRange(run: WorkflowRun, base: String, head: String) {
    val repo = GitHubRepo.parse(run.repo) ?: return
    val from = CommitSha.parse(base) ?: return
    val to = CommitSha.parse(head) ?: return
    followGitHub(repo) { name -> addSource(Source.CommitRange(name, from, to).ref) }
}

/**
 * Opens the file a log or annotation names, [target], at [run]'s commit: the repo's path when it is one, else the one
 * file whose name it ends with. Nothing opens when no file or several match.
 */
fun Workbench.openRunFile(run: WorkflowRun, target: String, line: Int?) {
    val repo = GitHubRepo.parse(run.repo) ?: return
    val revision = Revision(run.headSha)
    followGitHub(repo) { name ->
        scope.launch {
            val files = data.listFiles(name, revision)
            val clean = target.removePrefix("./").replace('\\', '/')
            val path = files.firstOrNull { it == clean } ?: files.filter { it.endsWith("/$clean") }.singleOrNull() ?: return@launch
            panes.openFile(name, revision, path)
        }
    }
}

/** Opens an HTML artifact in the browser from a folder it is unpacked into, since it is an untrusted page. */
fun Workbench.openArtifactPage(run: WorkflowRun, content: ArtifactContent) {
    scope.launch {
        val page = withContext(dispatchers.io) {
            val folder = Files.createTempDirectory(ARTIFACT_PAGES + run.id)
            if (content.entries.size == 1 && content.entries.single() == content.entry && !content.file.toString().endsWith(content.entry)) {
                folder.resolve(content.entry.substringAfterLast('/')).also { Files.write(it, content.bytes) }
            } else {
                ArtifactReader.unpack(content.file, folder, content.entry)
            }
        } ?: return@launch
        links.open(page.toUri().toString())
    }
}

/** Removes run filter [id] with its tabs. */
fun Workbench.removeRunFilter(id: String) {
    state.workspace.pages.filter { it is Page.Runs && it.filter == id }.forEach { panes.close(it.id.value) }
    runs.watch.removeFilter(id)
}

private const val ARTIFACT_PAGES = "arc-artifact-"
