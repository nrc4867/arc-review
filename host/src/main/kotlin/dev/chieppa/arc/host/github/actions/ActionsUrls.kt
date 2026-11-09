package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** The REST addresses for a repo's Actions, and the page sizes they are asked with. */
object ActionsUrls {
    const val RUNS_PAGE = 50
    const val PAGE = 100

    fun repo(repo: GitHubRepo): String = "${GitHubApi.REPOS}/${repo.fullName}"
    fun runs(repo: GitHubRepo): String = "${repo(repo)}/actions/runs?per_page=$RUNS_PAGE"
    fun run(repo: GitHubRepo, id: Long): String = "${repo(repo)}/actions/runs/$id"
    fun jobs(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/jobs?filter=latest&per_page=$PAGE"
    fun artifacts(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/artifacts?per_page=$PAGE"
    fun pendingDeployments(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/pending_deployments"
    fun approve(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/approve"
    fun rerun(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/rerun"
    fun rerunFailed(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/rerun-failed-jobs"
    fun cancel(repo: GitHubRepo, run: Long): String = "${run(repo, run)}/cancel"
    fun rerunJob(repo: GitHubRepo, job: Long): String = "${repo(repo)}/actions/jobs/$job/rerun"
    fun jobLog(repo: GitHubRepo, job: Long): String = "${repo(repo)}/actions/jobs/$job/logs"
    fun artifactZip(repo: GitHubRepo, artifact: Long): String = "${repo(repo)}/actions/artifacts/$artifact/zip"
    fun annotations(repo: GitHubRepo, job: Long): String = "${repo(repo)}/check-runs/$job/annotations?per_page=$PAGE"
    fun workflows(repo: GitHubRepo): String = "${repo(repo)}/actions/workflows?per_page=$PAGE"
    fun dispatch(repo: GitHubRepo, workflowFile: String): String = "${repo(repo)}/actions/workflows/${encode(workflowFile)}/dispatches"
    fun branches(repo: GitHubRepo): String = "${repo(repo)}/branches?per_page=$PAGE"
    fun environments(repo: GitHubRepo): String = "${repo(repo)}/environments?per_page=$PAGE"
    fun contents(repo: GitHubRepo, path: String, ref: String): String =
        "${repo(repo)}/contents/${path.split('/').joinToString("/", transform = ::encode)}?ref=${encode(ref)}"
    fun deployments(repo: GitHubRepo, environment: String): String = "${repo(repo)}/deployments?environment=${encode(environment)}&per_page=10"
    fun compare(repo: GitHubRepo, base: String, head: String): String = "${repo(repo)}/compare/${encode(base)}...${encode(head)}"
    fun openPullsFrom(repo: GitHubRepo, owner: String, branch: String): String =
        "${repo(repo)}/pulls?state=open&per_page=1&head=${encode("$owner:$branch")}"
    fun pullFiles(repo: GitHubRepo, number: Int): String = "${repo(repo)}/pulls/$number/files?per_page=$PAGE"

    private fun encode(text: String): String = URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20")
}
