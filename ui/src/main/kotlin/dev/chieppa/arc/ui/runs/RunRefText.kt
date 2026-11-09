package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_ref_branch
import dev.chieppa.arc.design.generated.run_ref_fork
import dev.chieppa.arc.design.generated.run_ref_pull
import org.jetbrains.compose.resources.stringResource

/** Where a run ran, short: its pull request and commit, a fork's branch, or its branch or tag and commit. */
@Composable
fun runRefText(run: WorkflowRun): String {
    val sha = run.headSha.take(SHORT_SHA)
    val name = run.repo.substringAfter('/')
    val pull = run.pulls.firstOrNull()
    return when {
        run.fromFork -> stringResource(Res.string.run_ref_fork, name, pull?.toString() ?: "?", "${run.headOwner}/$name")
        pull != null -> stringResource(Res.string.run_ref_pull, name, pull, sha)
        isTag(run) -> run.headBranch.orEmpty()
        else -> stringResource(Res.string.run_ref_branch, run.headBranch ?: sha, sha)
    }
}

/** Whether [run] ran for a pushed tag. */
fun isTag(run: WorkflowRun): Boolean = run.event == "push" && run.headBranch?.let { TAG.matches(it) } == true

private val TAG = Regex("^v?\\d+(\\.\\d+)+.*")
const val SHORT_SHA = 7
