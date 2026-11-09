package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.host.github.review.MutationPacer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * The writes a person makes to GitHub Actions, each only after they confirm it: starting a workflow, re-running all
 * of a run, its failed jobs or one job, cancelling, approving or rejecting a deployment, and approving a fork's run.
 * Writes are spaced by [pacer]. Every call throws [GitHubException] when GitHub refuses it.
 */
class ActionsWriter(private val client: GitHubClient, private val pacer: MutationPacer) {
    /** Starts [workflowFile] of [repo] on [ref] with [inputs], asking GitHub to answer with the new run's id. */
    suspend fun dispatch(repo: GitHubRepo, workflowFile: String, ref: String, inputs: Map<String, String>): DispatchResult {
        val body = buildJsonObject {
            put("ref", JsonPrimitive(ref))
            put("inputs", JsonObject(inputs.mapValues { JsonPrimitive(it.value) }))
            put("return_run_details", JsonPrimitive(true))
        }
        val response = post(ActionsUrls.dispatch(repo, workflowFile), body.toString())
        val runId = if (response.status == GitHubApi.OK) (client.parse(response.body) as? JsonObject).long("workflow_run_id") else null
        return DispatchResult(runId)
    }

    suspend fun rerun(repo: GitHubRepo, run: Long, debug: Boolean) {
        post(ActionsUrls.rerun(repo, run), debugBody(debug))
    }

    suspend fun rerunFailed(repo: GitHubRepo, run: Long, debug: Boolean) {
        post(ActionsUrls.rerunFailed(repo, run), debugBody(debug))
    }

    suspend fun rerunJob(repo: GitHubRepo, job: Long, debug: Boolean) {
        post(ActionsUrls.rerunJob(repo, job), debugBody(debug))
    }

    suspend fun cancel(repo: GitHubRepo, run: Long) {
        post(ActionsUrls.cancel(repo, run), EMPTY)
    }

    /** Approves, or rejects when not [approve], run [run]'s deployments to [environments], with [comment]. */
    suspend fun reviewDeployments(repo: GitHubRepo, run: Long, environments: List<Long>, approve: Boolean, comment: String) {
        val body = buildJsonObject {
            put("environment_ids", JsonArray(environments.map(::JsonPrimitive)))
            put("state", JsonPrimitive(if (approve) APPROVED else REJECTED))
            put("comment", JsonPrimitive(comment))
        }
        post(ActionsUrls.pendingDeployments(repo, run), body.toString())
    }

    /** Lets a fork's run [run], held for a maintainer, run. */
    suspend fun approveFork(repo: GitHubRepo, run: Long) {
        post(ActionsUrls.approve(repo, run), EMPTY)
    }

    private suspend fun post(url: String, body: String): ApiResponse {
        pacer.pace()
        val response = client.post(url, body)
        if (response.status !in GitHubApi.OK..OK_END) throw GitHubException(GitHubException.Reason.Failed, "GitHub refused it: ${response.status}")
        return response
    }

    private fun debugBody(debug: Boolean): String = buildJsonObject { put("enable_debug_logging", JsonPrimitive(debug)) }.toString()

    private companion object {
        const val EMPTY = "{}"
        const val APPROVED = "approved"
        const val REJECTED = "rejected"
        const val OK_END = 299
    }
}
