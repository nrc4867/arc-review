package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.avatars.AvatarCache
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.core.source.RepoName

/**
 * The conversation on GitHub: each source's repo is asked of [github] through the GitHub repo its clone comes from,
 * which [repoOf] gives, and pictures come through [avatars]. A failure reads as nothing, or as a refused write.
 */
class GitHubConversations(
    private val github: GitHub,
    private val avatars: AvatarCache,
    private val repoOf: suspend (RepoName) -> GitHubRepo?,
) : ConversationService {
    override suspend fun conversation(source: Source.PullRequest): HostedConversation? {
        val repo = repoOf(source.repo) ?: return null
        val read = attempt { github.conversation(repo, source) } ?: return null
        return HostedConversation(read.pullId, read.conversation)
    }

    override suspend fun react(source: Source, subjectId: String, reaction: Reaction, add: Boolean): Boolean =
        attempt { github.react(subjectId, reaction, add) } != null

    override suspend fun resolve(source: Source, threadId: String, resolved: Boolean): Boolean =
        attempt { github.resolve(threadId, resolved) } != null

    override suspend fun push(push: ReviewPush, hosted: HostedConversation?): PushOutcome {
        val repo = repoOf(push.source.repo) ?: return failAll(push, NO_REPO)
        val outcome = attempt {
            if (push.source is Source.PullRequest) {
                val pullId = hosted?.pullId ?: return@attempt failAll(push, NOT_READ)
                github.pushReview(repo, pullId, push)
            } else {
                github.commitComments(repo, push)
            }
        }
        return outcome ?: failAll(push, FAILED)
    }

    override suspend fun avatar(url: String): ByteArray? = avatars.bytes(url)

    private fun failAll(push: ReviewPush, why: String) = PushOutcome(failed = push.items.associate { it.comment.id to why })

    private suspend fun <T> attempt(block: suspend () -> T): T? = try {
        block()
    } catch (_: GitHubException) {
        null
    }

    private companion object {
        const val NO_REPO = "Not a GitHub repo"
        const val NOT_READ = "Pull request not read"
        const val FAILED = "GitHub gave no answer"
    }
}
