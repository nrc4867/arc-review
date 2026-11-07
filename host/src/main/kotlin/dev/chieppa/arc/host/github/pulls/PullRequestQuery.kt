package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonObject

/** The GraphQL document for one pull request by repo and number, and reading its answer. */
object PullRequestQuery {
    fun text(repo: GitHubRepo, number: Int): String =
        "query { repository(owner: ${SearchDocument.quote(repo.owner)}, name: ${SearchDocument.quote(repo.name)}) " +
            "{ pullRequest(number: $number) { ...${PullRequestFragment.NAME} } } }\n" + PullRequestFragment.TEXT

    fun parse(data: JsonObject): PullRequestSummary? = data.obj("repository").obj("pullRequest")?.let(PullRequestParser::parse)
}
