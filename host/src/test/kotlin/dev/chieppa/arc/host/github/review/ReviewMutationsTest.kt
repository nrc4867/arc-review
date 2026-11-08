package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.PushItem
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.conversation.ReviewSummary
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewMutationsTest {
    private val ok = """{"data":{"m":{"clientMutationId":null}}}"""

    private fun github(vararg answers: ApiResponse): Pair<GitHub, RecordedTransport> {
        val transport = RecordedTransport(answers.toMutableList())
        return GitHub(FakeGitHub.client(transport), MutationPacer(wait = {})) to transport
    }

    @Test
    fun reactionsAreAddedAndRemovedByContent() = runTest {
        val (github, transport) = github(ApiResponse(200, body = ok), ApiResponse(200, body = ok))
        github.react("IC_1", Reaction.ThumbsUp, add = true)
        github.react("IC_1", Reaction.Hooray, add = false)
        val (add, remove) = transport.requests.map(RequestBodies::parse)
        assertTrue("addReaction(input: \$input)" in add.query && "AddReactionInput!" in add.query)
        assertEquals("IC_1", RequestBodies.string(add.input, "subjectId"))
        assertEquals("THUMBS_UP", RequestBodies.string(add.input, "content"))
        assertTrue("removeReaction(" in remove.query)
        assertEquals("HOORAY", RequestBodies.string(remove.input, "content"))
    }

    @Test
    fun threadsAreResolvedAndOpenedAgain() = runTest {
        val (github, transport) = github(ApiResponse(200, body = ok), ApiResponse(200, body = ok))
        github.resolve("PRRT_1", resolved = true)
        github.resolve("PRRT_1", resolved = false)
        val (resolve, unresolve) = transport.requests.map(RequestBodies::parse)
        assertTrue("resolveReviewThread(" in resolve.query && "unresolve" !in resolve.query)
        assertTrue("unresolveReviewThread(" in unresolve.query)
        assertEquals("PRRT_1", RequestBodies.string(resolve.input, "threadId"))
    }

    @Test
    fun graphQlErrorsThrow() = runTest {
        val (github) = github(ApiResponse(200, body = """{"data":{"addReaction":null},"errors":[{"message":"Not found"}]}"""))
        val error = assertFailsWith<GitHubException> { github.react("X", Reaction.Eyes, add = true) }
        assertTrue("Not found" in error.message!!)
    }

    @Test
    fun commitCommentsArePostedOnTheHead() = runTest {
        val source = Source.Commit(RepoName("acme/api"), CommitSha("2222222"))
        val head = ObjectId("2222222222222222222222222222222222222222")
        val items = listOf(
            PushItem(LocalComment("on-line", source, LocalKind.Staged, "Here.", 0, CommentAnchor("src/a.kt", 5)), StagedRoute.Commit),
            PushItem(LocalComment("whole", source, LocalKind.Staged, "Overall.", 0), StagedRoute.Commit),
            PushItem(LocalComment("refused", source, LocalKind.Staged, "No.", 0), StagedRoute.Commit),
        )
        val (github, transport) = github(ApiResponse(201, body = "{}"), ApiResponse(201, body = "{}"), ApiResponse(422, body = "{}"))
        val outcome = github.commitComments(GitHubRepo("acme", "api"), ReviewPush(source, head, ReviewSummary(), items))

        assertEquals(setOf("on-line", "whole"), outcome.sent)
        assertEquals(setOf("refused"), outcome.failed.keys)
        assertFalse(outcome.submitted)
        val first = transport.requests.first()
        assertEquals("POST", first.method)
        assertEquals("https://api.github.com/repos/acme/api/commits/${head.value}/comments", first.url)
        val anchored = Json.parseToJsonElement(first.body!!).jsonObject
        assertEquals("src/a.kt", RequestBodies.string(anchored, "path"))
        assertEquals("5", anchored["line"].toString())
        assertEquals(setOf("body"), Json.parseToJsonElement(transport.requests[1].body!!).jsonObject.keys)
    }
}
