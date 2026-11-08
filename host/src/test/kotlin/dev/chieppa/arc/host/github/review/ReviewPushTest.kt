package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.PushItem
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.conversation.ReviewSummary
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPushTest {
    private val repo = GitHubRepo("acme", "api")
    private val source = Source.PullRequest(RepoName("acme/api"), 7)
    private val head = ObjectId("1111111111111111111111111111111111111111")
    private val waits = mutableListOf<Long>()

    private fun ok(): ApiResponse = ApiResponse(200, body = """{"data":{"m":{"clientMutationId":null}}}""")
    private fun noPending(): ApiResponse = ApiResponse(200, body = """{"data":{"node":{"reviews":{"nodes":[]}}}}""")
    private fun pending(id: String): ApiResponse = ApiResponse(200, body = """{"data":{"node":{"reviews":{"nodes":[{"id":"$id","viewerDidAuthor":true}]}}}}""")
    private fun created(id: String): ApiResponse = ApiResponse(200, body = """{"data":{"addPullRequestReview":{"pullRequestReview":{"id":"$id"}}}}""")
    private fun error(message: String): ApiResponse = ApiResponse(200, body = """{"data":null,"errors":[{"message":"$message"}]}""")

    private fun github(vararg answers: ApiResponse): Pair<GitHub, RecordedTransport> {
        val transport = RecordedTransport(answers.toMutableList())
        return GitHub(FakeGitHub.client(transport), MutationPacer(clock = { 0L }, wait = { waits += it })) to transport
    }

    private fun item(id: String, route: StagedRoute, anchor: CommentAnchor? = null, body: String = "Body $id", inReplyTo: String? = null, fromPending: Boolean = false) =
        PushItem(LocalComment(id, source, LocalKind.Staged, body, 0, anchor, inReplyTo, fromPending), route)

    private fun push(items: List<PushItem>, verdict: Verdict = Verdict.Comment, text: String = "", viewed: List<String> = emptyList()) =
        ReviewPush(source, head, ReviewSummary(verdict, text), items, viewed)

    private fun RecordedTransport.bodies() = requests.map(RequestBodies::parse)

    @Test
    fun withNoPendingReviewOneIsStartedThenThreadsThenSubmitted() = runTest {
        val (github, transport) = github(noPending(), created("PRR_new"), ok(), ok(), ok(), ok(), ok(), ok())
        val items = listOf(
            item("line", StagedRoute.Line, CommentAnchor("src/a.kt", 12, LineSide.New, 10)),
            item("old", StagedRoute.Line, CommentAnchor("src/a.kt", 4, LineSide.Old)),
            item("reply", StagedRoute.Reply, inReplyTo = "PRRT_1", body = "I \"agree\"\nreally"),
            item("talk", StagedRoute.Conversation),
        )
        val outcome = github.pushReview(repo, "PR_1", push(items, Verdict.Approve, viewed = listOf("src/a.kt")))

        assertEquals(setOf("line", "old", "reply", "talk"), outcome.sent)
        assertTrue(outcome.complete)
        assertEquals(listOf("src/a.kt"), outcome.viewed)
        val bodies = transport.bodies()
        assertEquals(
            listOf("node(id:", "addPullRequestReview(", "addPullRequestReviewThread(", "addPullRequestReviewThread(", "addPullRequestReviewThreadReply(", "submitPullRequestReview(", "addComment(", "markFileAsViewed("),
            bodies.map { body -> listOf("node(id:", "addPullRequestReviewThreadReply(", "addPullRequestReviewThread(", "addPullRequestReview(", "submitPullRequestReview(", "addComment(", "markFileAsViewed(").first { it in body.query } },
        )
        val start = bodies[1].input
        assertEquals("PR_1", RequestBodies.string(start, "pullRequestId"))
        assertEquals(head.value, RequestBodies.string(start, "commitOID"))
        assertFalse("event" in start)

        val line = bodies[2].input
        assertEquals("PRR_new", RequestBodies.string(line, "pullRequestReviewId"))
        assertEquals("12", line["line"].toString())
        assertEquals("10", line["startLine"].toString())
        assertEquals("RIGHT", RequestBodies.string(line, "side"))
        assertEquals("RIGHT", RequestBodies.string(line, "startSide"))
        assertEquals("LEFT", RequestBodies.string(bodies[3].input, "side"))
        assertFalse("startLine" in bodies[3].input)

        val reply = bodies[4]
        assertEquals("PRRT_1", RequestBodies.string(reply.input, "pullRequestReviewThreadId"))
        assertEquals("I \"agree\"\nreally", RequestBodies.string(reply.input, "body"))
        assertFalse("agree" in reply.query)

        assertEquals("APPROVE", RequestBodies.string(bodies[5].input, "event"))
        assertEquals("PR_1", RequestBodies.string(bodies[6].input, "subjectId"))
        assertEquals(bodies.size - 2, waits.size)
    }

    @Test
    fun anExistingPendingReviewIsReused() = runTest {
        val (github, transport) = github(pending("PRR_mine"), ok(), ok())
        val outcome = github.pushReview(repo, "PR_1", push(listOf(item("line", StagedRoute.Line, CommentAnchor("src/a.kt", 3)))))
        assertTrue(outcome.complete)
        val bodies = transport.bodies()
        assertEquals(3, bodies.size)
        assertFalse(bodies.any { "addPullRequestReview(" in it.query })
        assertEquals("PRR_mine", RequestBodies.string(bodies[1].input, "pullRequestReviewId"))
        assertEquals("COMMENT", RequestBodies.string(bodies[2].input, "event"))
    }

    @Test
    fun aFailingItemIsRecordedAndTheRestStillGo() = runTest {
        val (github, _) = github(pending("PRR_mine"), error("Line could not be resolved"), ok(), ok())
        val items = listOf(item("bad", StagedRoute.Line, CommentAnchor("src/a.kt", 300)), item("good", StagedRoute.Line, CommentAnchor("src/a.kt", 3)))
        val outcome = github.pushReview(repo, "PR_1", push(items))
        assertEquals(setOf("good"), outcome.sent)
        assertTrue("Line could not be resolved" in outcome.failed.getValue("bad"))
        assertTrue(outcome.submitted)
        assertFalse(outcome.complete)
    }

    @Test
    fun withNoReviewToAddToEveryItemFails() = runTest {
        val (github, _) = github(noPending(), error("Forbidden"))
        val outcome = github.pushReview(repo, "PR_1", push(listOf(item("a", StagedRoute.Line, CommentAnchor("x", 1)), item("b", StagedRoute.Conversation))))
        assertEquals(setOf("a", "b"), outcome.failed.keys)
        assertFalse(outcome.submitted)
        assertTrue(outcome.sent.isEmpty())
    }

    @Test
    fun aFailedSubmitLeavesTheReviewUnsubmitted() = runTest {
        val (github, _) = github(pending("PRR_mine"), error("Review is empty"))
        val outcome = github.pushReview(repo, "PR_1", push(listOf(item("sum", StagedRoute.Summary, CommentAnchor("x", 1)))))
        assertFalse(outcome.submitted)
        assertEquals(setOf("sum"), outcome.failed.keys)
    }

    @Test
    fun summaryAndFileCommentsCarryPermalinks() = runTest {
        val (github, transport) = github(pending("PRR_mine"), ok(), ok())
        val items = listOf(
            item("file", StagedRoute.File, CommentAnchor("src/a.kt", 40, startLine = 38), body = "Off the hunk."),
            item("sum", StagedRoute.Summary, CommentAnchor("lib/b.kt", 9), body = "Elsewhere."),
        )
        val outcome = github.pushReview(repo, "PR_1", push(items, Verdict.RequestChanges, "Overall fine."))
        assertEquals(setOf("file", "sum"), outcome.sent)
        val bodies = transport.bodies()
        val file = bodies[1].input
        assertEquals("FILE", RequestBodies.string(file, "subjectType"))
        assertFalse("line" in file)
        assertEquals(
            "> [src/a.kt:38-40](https://github.com/acme/api/blob/${head.value}/src/a.kt#L38-L40)\n\nOff the hunk.",
            RequestBodies.string(file, "body"),
        )
        val submit = bodies[2].input
        assertEquals("REQUEST_CHANGES", RequestBodies.string(submit, "event"))
        assertEquals(
            "Overall fine.\n\n**lib/b.kt:9** · Elsewhere. [permalink](https://github.com/acme/api/blob/${head.value}/lib/b.kt#L9)",
            RequestBodies.string(submit, "body"),
        )
    }

    @Test
    fun commentsAlreadyOnThePendingReviewAreNotSentAgain() = runTest {
        val (github, transport) = github(pending("PRR_mine"), ok())
        val outcome = github.pushReview(repo, "PR_1", push(listOf(item("p", StagedRoute.Line, CommentAnchor("x", 1), fromPending = true))))
        assertEquals(setOf("p"), outcome.sent)
        assertEquals(2, transport.requests.size)
        assertTrue("submitPullRequestReview(" in transport.bodies()[1].query)
    }

    @Test
    fun aRefusedViewedMarkDoesNotFailThePush() = runTest {
        val (github, _) = github(pending("PRR_mine"), ok(), error("FORBIDDEN"), ok())
        val outcome = github.pushReview(repo, "PR_1", push(emptyList(), viewed = listOf("a", "b")))
        assertTrue(outcome.complete)
        assertEquals(listOf("b"), outcome.viewed)
    }
}
