package dev.chieppa.arc.host.github.history

import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.pulls.SearchDocument
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonObject

/** The GraphQL document for a pull request's history, and reading its answer: its head, its latest force pushes and the commits its latest reviews were on. */
object PullHistoryQuery {
    private const val LAST = 100

    fun text(repo: GitHubRepo, number: Int): String =
        "query { repository(owner: ${SearchDocument.quote(repo.owner)}, name: ${SearchDocument.quote(repo.name)}) " +
            "{ pullRequest(number: $number) { headRefOid " +
            "timelineItems(last: $LAST, itemTypes: [HEAD_REF_FORCE_PUSHED_EVENT]) { filteredCount nodes { " +
            "... on HeadRefForcePushedEvent { createdAt beforeCommit { oid } afterCommit { oid } } } } " +
            "reviews(last: $LAST) { nodes { commit { oid } } } } } }"

    /** The history in [data], or null when there is no such pull request. */
    fun parse(data: JsonObject): PullHistory? {
        val pull = data.obj("repository").obj("pullRequest") ?: return null
        val pushes = pull.nodes("timelineItems").mapNotNull { event ->
            event.instant("createdAt")?.let { ForcePush(event.obj("beforeCommit").string("oid"), event.obj("afterCommit").string("oid"), it) }
        }
        val reviews = pull.nodes("reviews").mapNotNull { it.obj("commit").string("oid") }.distinct()
        return PullHistory(pull.string("headRefOid"), pushes.sortedBy { it.at }, reviews)
    }
}
