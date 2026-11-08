package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConversationRulesTest {
    private val repo = RepoName("api")
    private val pull = Source.PullRequest(repo, 412)

    private fun comment(anchor: CommentAnchor?, source: Source = pull, inReplyTo: String? = null) =
        LocalComment("c", source, LocalKind.Staged, "body", 0, anchor, inReplyTo)

    @Test
    fun `lines move with the text around them and changed lines are gone`() {
        val old = listOf("a", "b", "c", "d", "e")
        val new = listOf("x", "a", "b", "C", "d", "e")
        val map = LineMap.of(old, new)
        assertEquals(2, map.map(1))
        assertEquals(3, map.map(2))
        assertNull(map.map(3))
        assertEquals(5, map.map(4))
        assertEquals(6, map.map(5))
        assertNull(map.map(6))
    }

    @Test
    fun `comments go up by the route the host takes`() {
        val anchor = CommentAnchor("a.kt", 10)
        assertEquals(StagedRoute.Line, CommentRouting.route(comment(anchor), changed = true, commentable = setOf(9, 10, 11)))
        assertEquals(StagedRoute.File, CommentRouting.route(comment(anchor), changed = true, commentable = setOf(1, 2)))
        assertEquals(StagedRoute.Summary, CommentRouting.route(comment(anchor), changed = false, commentable = null))
        assertEquals(StagedRoute.Conversation, CommentRouting.route(comment(null), changed = false, commentable = null))
        assertEquals(StagedRoute.Reply, CommentRouting.route(comment(anchor, inReplyTo = "t"), changed = true, commentable = emptySet()))
        val commit = Source.Commit(repo, CommitSha("3fa9c1e"))
        assertEquals(StagedRoute.Commit, CommentRouting.route(comment(anchor, commit), changed = true, commentable = null))
    }

    @Test
    fun `a span is commented on its line only when every line of it is commentable`() {
        val span = CommentAnchor("a.kt", 12, startLine = 10)
        assertEquals(StagedRoute.File, CommentRouting.route(comment(span), changed = true, commentable = setOf(10, 12)))
        assertEquals(StagedRoute.Line, CommentRouting.route(comment(span), changed = true, commentable = setOf(10, 11, 12)))
    }

    @Test
    fun `toggling a reaction adds or takes away the person's own`() {
        val groups = listOf(ReactionGroup(Reaction.ThumbsUp, 2), ReactionGroup(Reaction.Heart, 1, mine = true))
        val liked = Reactions.toggle(groups, Reaction.ThumbsUp)
        assertEquals(ReactionGroup(Reaction.ThumbsUp, 3, mine = true), liked.first())
        assertEquals(listOf(ReactionGroup(Reaction.ThumbsUp, 2)), Reactions.toggle(groups, Reaction.Heart))
        assertEquals(Reaction.Eyes, Reactions.toggle(groups, Reaction.Eyes).last().reaction)
    }

    @Test
    fun `a push takes away what it sent and marks what failed`() {
        val a = comment(CommentAnchor("a.kt", 1)).copy(id = "a")
        val b = comment(CommentAnchor("a.kt", 2)).copy(id = "b")
        val note = comment(null).copy(id = "n", kind = LocalKind.Note)
        val local = LocalConversation(listOf(a, b, note), mapOf(pull to ReviewSummary(Verdict.Approve, "ok")))
        val after = local.afterPush(pull, PushOutcome(sent = setOf("a"), failed = mapOf("b" to "no"), submitted = false))
        assertEquals(listOf("b", "n"), after.comments.map { it.id })
        assertEquals(true, after.byId("b")?.failed)
        assertEquals(Verdict.Approve, after.summary(pull).verdict)
        assertEquals(ReviewSummary(), local.afterPush(pull, PushOutcome(sent = setOf("a", "b"), submitted = true)).summary(pull))
    }

    @Test
    fun `a moved anchor keeps its span`() {
        assertEquals(CommentAnchor("a.kt", 20, startLine = 18), CommentAnchor("a.kt", 12, startLine = 10).movedTo(20, null))
    }
}
