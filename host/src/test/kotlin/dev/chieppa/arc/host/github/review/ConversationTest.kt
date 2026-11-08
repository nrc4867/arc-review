package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.arc.core.conversation.ReviewState
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.FakeGitHub
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.RecordedTransport
import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConversationTest {
    private val repo = GitHubRepo("acme", "api")
    private val source = Source.PullRequest(RepoName("acme/api"), 7)

    private suspend fun read(body: String): Pair<HostConversation?, RecordedTransport> {
        val transport = RecordedTransport(mutableListOf(ApiResponse(200, body = body)))
        return GitHub(FakeGitHub.client(transport)).conversation(repo, source) to transport
    }

    private fun epoch(text: String): Long = Instant.parse(text).epochSecond

    @Test
    fun theRequestNamesThePullRequestInVariables() = runTest {
        val (_, transport) = read(FakeGitHub.resource("conversation.json"))
        val body = RequestBodies.parse(transport.requests.single())
        assertEquals("acme", RequestBodies.string(body.variables, "owner"))
        assertEquals("7", body.variables["number"].toString())
        assertFalse("acme" in body.query)
    }

    @Test
    fun theDescriptionAndIdsComeBack() = runTest {
        val host = read(FakeGitHub.resource("conversation.json")).first!!
        assertEquals("PR_kwDO1", host.pullId)
        assertEquals("1111111111111111111111111111111111111111", host.headOid)
        assertEquals("PRR_3", host.pendingReviewId)
        assertEquals("me", host.conversation.viewer)
        val description = host.conversation.description!!
        assertEquals("alice", description.author)
        assertEquals("Adds the thing.", description.body)
        assertEquals("PR_kwDO1", description.id)
        assertEquals("https://avatars.githubusercontent.com/u/1?u=abc&v=4", description.avatar)
        assertEquals(listOf(ReactionGroup(Reaction.ThumbsUp, 2, mine = true), ReactionGroup(Reaction.Rocket, 1)), description.reactions)
    }

    @Test
    fun entriesHoldCommentsAndReviewsThatSaySomethingOrLeftCommentsOldestFirst() = runTest {
        val read = read(FakeGitHub.resource("conversation.json")).first!!.conversation
        val entries = read.entries.filter { it.message.id != "PRR_2" }
        assertEquals(listOf("IC_1", "PRR_1", "PRR_2", "IC_2", "PRR_4"), read.entries.map { it.message.id })
        assertEquals("PRR_2", read.threads.flatMap { it.messages }.single { it.id == "PRRC_1" }.review)
        val approved = entries[1] as ConversationEntry.Review
        assertEquals(ReviewState.Approved, approved.state)
        assertEquals(epoch("2026-11-02T10:00:00Z"), approved.message.writtenAt)
        val ghost = entries[2] as ConversationEntry.Comment
        assertEquals("ghost", ghost.message.author)
        assertNull(ghost.message.avatar)
        val changes = entries[3] as ConversationEntry.Review
        assertEquals(ReviewState.ChangesRequested, changes.state)
        assertEquals(2, changes.comments)
        assertEquals(listOf(ReactionGroup(Reaction.Eyes, 1)), changes.message.reactions)
    }

    @Test
    fun threadsReadTheirStateLineAndSide() = runTest {
        val threads = read(FakeGitHub.resource("conversation.json")).first!!.conversation.threads.associateBy { it.id }
        assertEquals(setOf("PRRT_open", "PRRT_outdated", "PRRT_resolved", "PRRT_file"), threads.keys)

        val open = threads.getValue("PRRT_open")
        assertEquals(ThreadState.Open, open.state)
        assertEquals(12, open.line)
        assertEquals(10, open.startLine)
        assertEquals(LineSide.New, open.side)
        assertEquals(ObjectId("3333333333333333333333333333333333333333"), open.madeOn)
        assertEquals(source, open.source)
        assertEquals(listOf("PRRC_1"), open.messages.map { it.id })
        assertEquals(listOf(ReactionGroup(Reaction.Laugh, 1)), open.messages.single().reactions)

        val outdated = threads.getValue("PRRT_outdated")
        assertEquals(ThreadState.Addressed, outdated.state)
        assertEquals(7, outdated.line)
        assertEquals(5, outdated.startLine)
        assertEquals(LineSide.Old, outdated.side)
        assertEquals("ghost", outdated.messages.single().author)

        assertEquals(ThreadState.Resolved, threads.getValue("PRRT_resolved").state)

        val file = threads.getValue("PRRT_file")
        assertEquals(1, file.line)
        assertNull(file.startLine)
        assertNull(file.madeOn)
    }

    @Test
    fun pendingCommentsComeBackStagedNotInThreads() = runTest {
        val pending = read(FakeGitHub.resource("conversation.json")).first!!.conversation.pending.associateBy { it.id }
        assertEquals(setOf("PRRC_2", "PRRC_6"), pending.keys)
        val head = ObjectId("1111111111111111111111111111111111111111")

        val reply = pending.getValue("PRRC_2")
        assertEquals("PRRT_open", reply.inReplyTo)
        assertTrue(reply.fromPending)
        assertEquals(LocalKind.Staged, reply.kind)
        assertEquals(CommentAnchor("src/a.kt", 12, LineSide.New, null, head), reply.anchor)
        assertEquals(epoch("2026-11-04T09:30:00Z"), reply.writtenAt)

        val fresh = pending.getValue("PRRC_6")
        assertNull(fresh.inReplyTo)
        assertEquals("A new thought.", fresh.body)
        assertEquals(CommentAnchor("src/d.kt", 20, LineSide.New, 18, head), fresh.anchor)
        assertEquals(source, fresh.source)
    }

    @Test
    fun aMissingPullRequestHasNoConversation() = runTest {
        assertNull(read("""{"data":{"viewer":{"login":"me"},"repository":{"pullRequest":null}}}""").first)
    }
}
