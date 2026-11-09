package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.Workflow
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.io.InputStream

/**
 * Reads a repo's GitHub Actions over REST: its latest runs and a run's jobs conditionally, so an unchanged list costs
 * nothing, and a run, its artifacts, annotations and pending deployments, the workflows and branches, a file at a ref,
 * what a deployment ships and what a fork's pull request changes under `.github/`. Logs and artifacts are streamed.
 * Every call throws [GitHubException] when GitHub gives nothing to use.
 */
class ActionsClient(private val client: GitHubClient) {
    /** The latest runs of [repo], or unchanged since [etag]. */
    suspend fun runs(repo: GitHubRepo, etag: String?): Conditional<List<WorkflowRun>> {
        val response = client.get(ActionsUrls.runs(repo), etag)
        if (response.status == GitHubApi.NOT_MODIFIED) return Conditional(null, etag)
        return Conditional(RunParser.list(obj(ok(response))), response.header(GitHubApi.ETAG))
    }

    suspend fun run(repo: GitHubRepo, id: Long): WorkflowRun? {
        val response = client.get(ActionsUrls.run(repo, id))
        if (response.status == GitHubApi.NOT_FOUND) return null
        return RunParser.run(obj(ok(response)))
    }

    /** The latest attempt's jobs of run [run], or unchanged since [etag]. */
    suspend fun jobs(repo: GitHubRepo, run: Long, etag: String?): Conditional<List<RunJob>> {
        val response = client.get(ActionsUrls.jobs(repo, run), etag)
        if (response.status == GitHubApi.NOT_MODIFIED) return Conditional(null, etag)
        return Conditional(JobParser.list(obj(ok(response))), response.header(GitHubApi.ETAG))
    }

    suspend fun artifacts(repo: GitHubRepo, run: Long): List<RunArtifact> = ArtifactParser.list(obj(ok(client.get(ActionsUrls.artifacts(repo, run)))))

    suspend fun pendingDeployments(repo: GitHubRepo, run: Long): List<PendingDeployment> =
        DeploymentParser.list(json(ok(client.get(ActionsUrls.pendingDeployments(repo, run)))))

    /** The annotations of [job]; a job id is its check run's id. */
    suspend fun annotations(repo: GitHubRepo, job: RunJob): List<RunAnnotation> =
        AnnotationParser.list(json(ok(client.get(ActionsUrls.annotations(repo, job.id)))), job.name)

    suspend fun workflows(repo: GitHubRepo): List<Workflow> = obj(ok(client.get(ActionsUrls.workflows(repo)))).objects("workflows")
        .filter { it.string("state") == ACTIVE }
        .mapNotNull { Workflow(it.long("id") ?: return@mapNotNull null, it.string("name").orEmpty(), it.string("path").orEmpty()) }

    suspend fun branches(repo: GitHubRepo): List<Branch> = (json(ok(client.get(ActionsUrls.branches(repo)))) as? JsonArray).orEmpty().mapNotNull { item ->
        val json = item as? JsonObject ?: return@mapNotNull null
        Branch(json.string("name") ?: return@mapNotNull null, json.obj("commit").string("sha").orEmpty())
    }

    suspend fun environments(repo: GitHubRepo): List<String> =
        obj(ok(client.get(ActionsUrls.environments(repo)))).objects("environments").mapNotNull { it.string("name") }

    /** The text of [path] at [ref] in [repo], or null when there is no such file. */
    suspend fun file(repo: GitHubRepo, path: String, ref: String): String? {
        val response = client.get(ActionsUrls.contents(repo, path, ref), accept = RAW)
        if (response.status == GitHubApi.NOT_FOUND) return null
        return ok(response).body
    }

    /** Whether the person may push to [repo], and so approve a fork's runs there. */
    suspend fun canPush(repo: GitHubRepo): Boolean {
        val permissions = obj(ok(client.get(ActionsUrls.repo(repo)))).obj("permissions")
        return PUSH_PERMISSIONS.any { permissions.boolean(it) == true }
    }

    /** The commits a deployment of [head] to [environment] ships since the environment's last other deployment. */
    suspend fun shipsSince(repo: GitHubRepo, environment: String, head: String): ShipsSince? {
        val deployments = (json(ok(client.get(ActionsUrls.deployments(repo, environment)))) as? JsonArray).orEmpty()
        val base = deployments.mapNotNull { (it as? JsonObject).string("sha") }.firstOrNull { it != head } ?: return null
        val compare = obj(ok(client.get(ActionsUrls.compare(repo, base, head))))
        val changes = compare.objects("commits").map { commit ->
            val title = commit.obj("commit").string("message").orEmpty().lineSequence().first()
            ShippedChange(commit.string("sha").orEmpty(), title, pullOf(title))
        }.reversed()
        return ShipsSince(base, head, compare.int("total_commits") ?: changes.size, changes)
    }

    /** What the open pull request from [owner]'s [branch] changes under `.github/`, or null when there is none. */
    suspend fun forkChanges(repo: GitHubRepo, owner: String, branch: String): ForkChanges? {
        val pulls = (json(ok(client.get(ActionsUrls.openPullsFrom(repo, owner, branch)))) as? JsonArray).orEmpty()
        val number = (pulls.firstOrNull() as? JsonObject).int("number") ?: return null
        val files = (json(ok(client.get(ActionsUrls.pullFiles(repo, number)))) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        val changes = files.mapNotNull { file ->
            val path = file.string("filename") ?: return@mapNotNull null
            if (!path.startsWith(GITHUB_FOLDER)) return@mapNotNull null
            ForkChange(path, file.string("status").orEmpty(), file.string("patch"))
        }
        return ForkChanges(number, changes)
    }

    /** Streams [job]'s log, finished jobs only, to [read]; null when GitHub has no log for it. */
    suspend fun <T> jobLog(repo: GitHubRepo, job: Long, read: (InputStream) -> T): T? =
        client.stream(ActionsUrls.jobLog(repo, job)) { status, body -> if (status == GitHubApi.OK) read(body) else null }

    /** Streams artifact [artifact]'s zip to [read]; null when it has expired or cannot be had. */
    suspend fun <T> artifact(repo: GitHubRepo, artifact: Long, read: (InputStream) -> T): T? =
        client.stream(ActionsUrls.artifactZip(repo, artifact)) { status, body -> if (status == GitHubApi.OK) read(body) else null }

    private fun ok(response: ApiResponse): ApiResponse {
        if (response.status !in GitHubApi.OK..OK_END) throw GitHubException(GitHubException.Reason.Failed, "Actions answered ${response.status}")
        return response
    }

    private fun json(response: ApiResponse): JsonElement = client.parse(response.body)

    private fun obj(response: ApiResponse): JsonObject =
        json(response) as? JsonObject ?: throw GitHubException(GitHubException.Reason.Failed, "Actions answer is not an object")

    private fun pullOf(title: String): Int? = (SQUASHED.find(title) ?: MERGED.find(title))?.groupValues?.get(1)?.toIntOrNull()

    private companion object {
        const val ACTIVE = "active"
        const val RAW = "application/vnd.github.raw+json"
        const val GITHUB_FOLDER = ".github/"
        const val OK_END = 299
        val PUSH_PERMISSIONS = listOf("push", "maintain", "admin")
        val SQUASHED = Regex("\\(#(\\d+)\\)\\s*$")
        val MERGED = Regex("^Merge pull request #(\\d+)")
    }
}
