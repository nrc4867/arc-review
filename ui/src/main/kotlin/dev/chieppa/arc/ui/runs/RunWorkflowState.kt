package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.actions.InputKind
import dev.chieppa.arc.core.actions.Workflow
import dev.chieppa.arc.core.actions.WorkflowInput
import dev.chieppa.arc.core.actions.WorkflowSpec
import dev.chieppa.arc.host.github.actions.Branch
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ActionsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The Run workflow form: the repo, workflow and branch picked, with the workflow's file read at the branch's head to
 * build the form from its manual trigger's inputs, and each input's value. [repos] are those it offers. Picking
 * another repo or workflow reads again; [preferredPath] picks a workflow by its file once the repo's are read.
 */
class RunWorkflowState(
    private val service: ActionsService,
    private val scope: CoroutineScope,
    repo: GitHubRepo?,
    private val preferredPath: String? = null,
    val repos: Set<GitHubRepo> = emptySet(),
) {
    var repo by mutableStateOf(repo)
        private set
    var workflows by mutableStateOf<List<Workflow>>(emptyList())
        private set
    var branches by mutableStateOf<List<Branch>>(emptyList())
        private set
    var environments by mutableStateOf<List<String>>(emptyList())
        private set
    var workflow by mutableStateOf<Workflow?>(null)
        private set
    var branch by mutableStateOf<Branch?>(null)
        private set
    var spec by mutableStateOf<WorkflowSpec?>(null)
        private set
    var reading by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
    val values = mutableStateMapOf<String, String>()
    private var load: Job? = null

    init {
        repo?.let(::pickRepo)
    }

    val inputs: List<WorkflowInput> get() = spec?.dispatchInputs.orEmpty()

    /** Whether the form can go: a workflow with a manual trigger, and every required input filled. */
    val ready: Boolean get() = spec?.dispatchable == true && branch != null && inputs.all { !it.required || !values[it.name].isNullOrBlank() }

    /** Each input's value as it goes to GitHub, blanks left out. */
    fun values(): Map<String, String> = inputs.mapNotNull { input -> values[input.name]?.takeIf { it.isNotBlank() }?.let { input.name to it } }.toMap()

    fun pickRepo(picked: GitHubRepo) {
        repo = picked
        workflow = null
        branch = null
        spec = null
        load?.cancel()
        load = scope.launch {
            reading = true
            try {
                workflows = service.workflows(picked)
                branches = service.branches(picked)
                environments = service.environments(picked)
                failed = false
            } catch (_: GitHubException) {
                failed = true
            }
            workflow = workflows.firstOrNull { it.path == preferredPath } ?: workflows.firstOrNull()
            branch = branches.firstOrNull { it.name == DEFAULT_BRANCH } ?: branches.firstOrNull()
            readSpec()
        }
    }

    fun pickWorkflow(picked: Workflow) {
        workflow = picked
        reload()
    }

    fun pickBranch(picked: Branch) {
        branch = picked
        reload()
    }

    private fun reload() {
        load?.cancel()
        load = scope.launch { readSpec() }
    }

    private suspend fun readSpec() {
        val repo = repo ?: return
        val path = workflow?.path ?: return
        val ref = branch?.sha?.ifEmpty { null } ?: branch?.name ?: return
        reading = true
        spec = try {
            service.workflowSpec(repo, path, ref)
        } catch (_: GitHubException) {
            null
        }
        reading = false
        values.clear()
        inputs.forEach { input ->
            val value = input.default ?: when (input.kind) {
                InputKind.Boolean -> false.toString()
                InputKind.Choice -> input.options.firstOrNull()
                InputKind.Environment -> environments.firstOrNull()
                else -> null
            }
            value?.let { values[input.name] = it }
        }
    }

    private companion object {
        const val DEFAULT_BRANCH = "main"
    }
}
