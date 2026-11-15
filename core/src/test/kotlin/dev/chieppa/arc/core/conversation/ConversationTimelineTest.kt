package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversationTimelineTest {
    private val pull = Source.PullRequest(RepoName("api"), 412)

    private fun thread(id: String, path: String, line: Int, at: Long, review: String?) =
        ReviewThread(id, pull, path, line, LineSide.New, ThreadState.Open, listOf(ThreadMessage("ana", "on $line", at, id = "m$id", review = review)))

    private val conversation = PullConversation(
        description = ThreadMessage("bo", "Adds caching", 1, id = "PR"),
        entries = listOf(
            ConversationEntry.Comment(ThreadMessage("ana", "Looks big", 10, id = "C1")),
            ConversationEntry.Review(ThreadMessage("ana", "", 30, id = "R1"), ReviewState.ChangesRequested, comments = 2),
        ),
        threads = listOf(
            thread("T2", "b.kt", 9, 29, "R1"),
            thread("T1", "a.kt", 40, 28, "R1"),
            thread("T3", "a.kt", 3, 20, "R-gone"),
        ),
    )

    @Test
    fun `reviews hold the threads they started and other threads stand at their time`() {
        val items = ConversationTimeline.inOrder(conversation)
        assertEquals(listOf("description", "C1", "T3", "R1"), items.map { it.key })
        val review = items.last() as TimelineItem.Entry
        assertEquals(listOf("T1", "T2"), review.threads.map { it.id })
    }

    @Test
    fun `threads group by file in path and line order`() {
        val items = ConversationTimeline.byFile(conversation)
        assertEquals(listOf("description", "file:a.kt", "file:b.kt"), items.map { it.key })
        assertEquals(listOf("T3", "T1"), (items[1] as TimelineItem.File).threads.map { it.id })
    }

    @Test
    fun `commits with nothing between them make one item and a force push stands alone`() {
        fun commit(sha: String, at: Long) = PullCommit(sha, "Change $sha", "bo", at)
        val withCommits = conversation.copy(
            commits = listOf(commit("a1", 2), commit("a2", 3), commit("a3", 15), commit("a4", 40), commit("a5", 42)),
            forcePushes = listOf(HeadForcePush("bo", "a4", "a5", 41)),
        )
        val items = ConversationTimeline.inOrder(withCommits)
        assertEquals(
            listOf("description", "commits:a1", "C1", "commits:a3", "T3", "R1", "commits:a4", "force-push:41:a5", "commits:a5"),
            items.map { it.key },
        )
        assertEquals(listOf("a1", "a2"), (items[1] as TimelineItem.Commits).commits.map { it.sha })
    }

    @Test
    fun `by file leaves commits out`() {
        val items = ConversationTimeline.byFile(conversation.copy(commits = listOf(PullCommit("a1", "x", "bo", 2))))
        assertEquals(listOf("description", "file:a.kt", "file:b.kt"), items.map { it.key })
    }
}
