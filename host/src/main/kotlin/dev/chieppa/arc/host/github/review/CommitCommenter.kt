package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Pushes a staged batch for a source that is not a pull request: every comment goes up as a comment on the push's
 * head commit, on its line when it has one. There is no review to submit, so the outcome counts as submitted when
 * every comment went.
 */
class CommitCommenter(private val client: GitHubClient, private val pacer: MutationPacer) {
    suspend fun push(repo: GitHubRepo, push: ReviewPush): PushOutcome {
        val sha = push.head?.value ?: return PushOutcome(failed = push.items.associate { it.comment.id to NO_HEAD })
        val url = url(repo, sha)
        val sent = linkedSetOf<String>()
        val failed = linkedMapOf<String, String>()
        for (item in push.items) {
            val body = buildJsonObject {
                put(BODY, JsonPrimitive(item.comment.body))
                item.anchor?.let { anchor ->
                    put(PATH, JsonPrimitive(anchor.path))
                    put(LINE, JsonPrimitive(anchor.line))
                }
            }
            try {
                pacer.pace()
                val response = client.post(url, body.toString())
                if (response.status == GitHubApi.CREATED) sent += item.comment.id else failed[item.comment.id] = "$REFUSED ${response.status}"
            } catch (e: GitHubException) {
                failed[item.comment.id] = e.message ?: e.reason.name
            }
        }
        return PushOutcome(sent, failed, submitted = failed.isEmpty())
    }

    /** The REST address for comments on commit [sha] of [repo]. */
    fun url(repo: GitHubRepo, sha: String): String = "${GitHubApi.REPOS}/${repo.owner}/${repo.name}/commits/$sha/comments"

    private companion object {
        const val BODY = "body"
        const val PATH = "path"
        const val LINE = "line"
        const val NO_HEAD = "No commit to comment on"
        const val REFUSED = "GitHub answered"
    }
}
