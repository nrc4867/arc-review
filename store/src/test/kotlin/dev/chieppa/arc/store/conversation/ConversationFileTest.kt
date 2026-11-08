package dev.chieppa.arc.store.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalComment
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.ReviewSummary
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ConversationFileTest {
    private val path = Files.createTempDirectory("arc-conversation").resolve("conversation.json")
    private val pull = Source.PullRequest(RepoName("api"), 412)

    @Test
    fun draftsStagedCommentsNotesAndSummariesComeBackAsSaved() {
        val anchor = CommentAnchor("src/a.kt", 12, LineSide.Old, startLine = 10, madeOn = ObjectId("a".repeat(40)))
        val conversation = LocalConversation(
            listOf(
                LocalComment("d1", pull, LocalKind.Draft, "draft", 1, anchor),
                LocalComment("s1", pull, LocalKind.Staged, "reply", 2, CommentAnchor("src/a.kt", 3), inReplyTo = "T_1", failed = true),
                LocalComment("n1", pull, LocalKind.Note, "free note", 3),
            ),
            mapOf(pull to ReviewSummary(Verdict.RequestChanges, "Two blockers.")),
        )
        ConversationFile(path).save(conversation)
        assertEquals(conversation, ConversationFile(path).load())
    }

    @Test
    fun commentsFromAPendingReviewStayOnTheHost() {
        val pending = LocalComment("p1", pull, LocalKind.Staged, "from the web", 1, CommentAnchor("a.kt", 1), fromPending = true)
        ConversationFile(path).save(LocalConversation(listOf(pending)))
        assertEquals(LocalConversation(), ConversationFile(path).load())
    }

    @Test
    fun aMissingFileIsAnEmptyConversation() {
        assertEquals(LocalConversation(), ConversationFile(path).load())
    }
}
