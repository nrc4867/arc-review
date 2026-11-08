package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.LocalKind
import dev.chieppa.arc.core.conversation.PullConversation
import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReactionGroup
import dev.chieppa.arc.core.conversation.ReviewState
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.conversation.ThreadMessage
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.services.HostedConversation
import dev.chieppa.arc.ui.conversation.LineChoice
import dev.chieppa.arc.ui.conversation.ThreadJumps
import dev.chieppa.arc.ui.conversation.ThreadReveal
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ConversationUiTest {
    private val data = FakeReviewData()
    private val host = FakeConversations()

    @Test
    fun aCommentIsWrittenStagedAndKeptAsANoteHere() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 20, madeOn = data.head))
        controller.write(id, "  Why twenty?  ")
        controller.stage(id)
        assertEquals(LocalKind.Staged, controller.local.byId(id)?.kind)
        assertEquals("Why twenty?", controller.local.byId(id)?.body)
        controller.edit(id)
        controller.keepAsNote(id)
        assertEquals(LocalKind.Note, controller.local.byId(id)?.kind)
        val empty = controller.startComment(data.source, CommentAnchor("app/Main.kt", 3))
        controller.close(empty)
        assertNull(controller.local.byId(empty))
    }

    @Test
    fun aStagedCommentOnACommitRangeGoesUpOnItsCommit() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 41, madeOn = data.head))
        controller.write(id, "Found it")
        controller.stage(id)
        waitForIdle()
        val staged = workbench.comments.pusher.staged(data.source).single()
        assertEquals(StagedRoute.Commit, staged.route)
        assertEquals(41, staged.line)
        workbench.comments.pusher.push(data.source)
        assertWhenIdle { host.pushes.isNotEmpty() && workbench.comments.pusher.pushing == null }
        assertEquals(data.head, host.pushes.single().head)
        assertTrue(controller.local.staged(data.source).isEmpty())
    }

    @Test
    fun aCommentWrittenAtAnOlderHeadFollowsItsLineOrIsMarkedGone() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val kept = controller.startComment(data.source, CommentAnchor("app/Main.kt", 25, madeOn = data.base)).also { controller.write(it, "kept"); controller.stage(it) }
        val gone = controller.startComment(data.source, CommentAnchor("app/Main.kt", 20, madeOn = data.base)).also { controller.write(it, "gone"); controller.stage(it) }
        assertWhenIdle { workbench.comments.placer.placed.keys.containsAll(listOf(kept, gone)) }
        val rows = workbench.comments.pusher.staged(data.source).associateBy { it.id }
        assertEquals(25, rows.getValue(kept).line)
        assertNull(rows.getValue(kept).lineGoneIn)
        assertEquals(data.head.short(), rows.getValue(gone).lineGoneIn)
        assertTrue(workbench.comments.pusher.plan(data.source).items.none { it.comment.id == gone })
    }

    @Test
    fun aFailedPushLeavesItsCommentsStagedAndMarked() = runComposeUiTest {
        host.outcome = { push -> PushOutcome(failed = push.items.associate { it.comment.id to "no" }) }
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 41)).also { controller.write(it, "one"); controller.stage(it) }
        workbench.comments.pusher.push(data.source)
        assertWhenIdle { host.pushes.isNotEmpty() && workbench.comments.pusher.pushing == null }
        assertEquals(true, controller.local.byId(id)?.failed)
        assertTrue(workbench.comments.pusher.staged(data.source).single().failed)
    }

    @Test
    fun reactionsAndResolvingShowAtOnceAndGoBackWhenTheHostRefuses() = runComposeUiTest {
        val pull = Source.PullRequest(data.repo, 7)
        data.pulls[pull] = FakePulls.pull(7, "Seven")
        val message = ThreadMessage("mfischer", "Same transaction?", 0, id = "C_1", reactions = listOf(ReactionGroup(Reaction.ThumbsUp, 1)))
        val thread = ReviewThread("T_1", pull, "app/Main.kt", 20, LineSide.New, ThreadState.Open, listOf(message))
        host.hosted = HostedConversation("PR_7", PullConversation(entries = listOf(ConversationEntry.Comment(message.copy(id = "C_2"))), threads = listOf(thread), viewer = "me"))
        host.accepts = false
        val workbench = openReview(data, conversations = host)
        assertTrue(workbench.addSource(pull.ref))
        assertWhenIdle { workbench.comments.controller.hosted[pull] != null }
        val controller = workbench.comments.controller
        val answer = CompletableDeferred<Unit>().also { host.answer = it }
        controller.react(pull, "C_1", Reaction.ThumbsUp)
        assertEquals(2, controller.hostThreads(pull, "app/Main.kt").single().messages.single().reactions.single().count)
        answer.complete(Unit)
        host.answer = null
        assertWhenIdle { controller.hostThreads(pull, "app/Main.kt").single().messages.single().reactions.single().count == 1 }
        assertEquals(Triple("C_1", Reaction.ThumbsUp, true), host.reactions.single())
        host.accepts = true
        controller.resolve(controller.hostThreads(pull, "app/Main.kt").single(), true)
        assertWhenIdle { host.resolved.isNotEmpty() }
        waitForIdle()
        assertEquals(ThreadState.Resolved, controller.hostThreads(pull, "app/Main.kt").single().state)
    }

    @Test
    fun aStagedCommentJumpedToOpensUnderItsLine() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 41)).also { controller.write(it, "Look here"); controller.stage(it) }
        waitForIdle()
        assertTrue(!shows("Edit comment"))
        assertTrue(ThreadJumps.toLocal(workbench, id))
        waitForIdle()
        assertNull(workbench.threadViews.reveal)
        assertTrue(shows("Edit comment"))
        onNodeWithText("Edit comment").performClick()
        controller.stage(id)
        waitForIdle()
        assertTrue(!shows("Edit comment"))
    }

    @Test
    fun aClickOnALineWithThreadsAsksWhetherToStartOneOrOpenOne() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 41)).also { controller.write(it, "Look here"); controller.stage(it) }
        assertTrue(workbench.panes.openChange(data.source, "app/Main.kt"))
        waitForIdle()
        val choice = LineChoice(workbench.panes.focused!!.id.value, LineSide.New, 41)
        workbench.threadViews.reveal = ThreadReveal(data.source, "app/Main.kt", LineSide.New, 41)
        workbench.threadViews.choosing = choice
        waitForIdle()
        onNodeWithText("Staged · You: Look here").performClick()
        waitForIdle()
        assertNull(workbench.threadViews.choosing)
        assertTrue(shows("Edit comment"))
        workbench.threadViews.choosing = choice
        waitForIdle()
        onNodeWithText("New comment").performClick()
        waitForIdle()
        assertEquals(2, controller.local.of(data.source).size)
        assertTrue(controller.local.of(data.source).any { it.id != id && it.anchor?.line == 41 })
    }

    @Test
    fun theConversationShowsEachReviewsThreadsWhereTheyAreAndOpensAsATab() = runComposeUiTest {
        val pull = Source.PullRequest(data.repo, 7)
        data.pulls[pull] = FakePulls.pull(7, "Seven")
        val open = ThreadMessage("mfischer", "Same transaction?", 5, id = "C_1", review = "R_1")
        val done = ThreadMessage("mfischer", "Old point", 4, id = "C_9", review = "R_1")
        val review = ConversationEntry.Review(ThreadMessage("mfischer", "", 6, id = "R_1"), ReviewState.ChangesRequested, comments = 2)
        val threads = listOf(
            ReviewThread("T_1", pull, "app/Main.kt", 20, LineSide.New, ThreadState.Open, listOf(open)),
            ReviewThread("T_2", pull, "app/Main.kt", 3, LineSide.New, ThreadState.Resolved, listOf(done)),
        )
        host.hosted = HostedConversation("PR_7", PullConversation(ThreadMessage("bo", "Seven things", 1, id = "PR_7"), listOf(review), threads, viewer = "me"))
        val workbench = openReview(data, conversations = host)
        assertTrue(workbench.addSource(pull.ref))
        assertWhenIdle { workbench.comments.controller.hosted[pull] != null }
        if (workbench.frameLayout.drawers.firstOrNull { it.id == dev.chieppa.arc.ui.drawers.DrawerIds.CONVERSATION }?.open != true) {
            workbench.toggleDrawer(dev.chieppa.arc.ui.drawers.DrawerIds.CONVERSATION)
        }
        waitForIdle()
        // The drawer is short in a test window: fold the description to bring the review into view.
        workbench.conversationViews.toggleFold("description")
        waitForIdle()
        assertTrue(shows("Same transaction?"))
        assertTrue(shows("app/Main.kt:20"))
        assertTrue(!shows("Old point"))
        assertTrue(shows("Seven things"))
        workbench.conversationViews.grouping = dev.chieppa.arc.ui.conversation.ConversationGrouping.ByFile
        waitForIdle()
        assertTrue(shows("R 20"))
        workbench.panes.openConversation(pull)
        waitForIdle()
        assertTrue(workbench.state.workspace.pages.any { it is dev.chieppa.arc.core.pane.Page.Conversation && it.source == pull })
    }

    @Test
    fun theStagedDrawerAsksBeforeAPush() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        controller.startComment(data.source, CommentAnchor("app/Main.kt", 41)).also { controller.write(it, "Ship it"); controller.stage(it) }
        waitForIdle()
        if (workbench.frameLayout.drawers.firstOrNull { it.id == dev.chieppa.arc.ui.drawers.DrawerIds.STAGED_COMMENTS }?.open != true) {
            workbench.toggleDrawer(dev.chieppa.arc.ui.drawers.DrawerIds.STAGED_COMMENTS)
        }
        waitForIdle()
        assertTrue(shows("Ship it"))
        onNodeWithText("Push comments").performClick()
        waitForIdle()
        assertEquals(data.source, workbench.comments.pusher.confirming)
        assertTrue(host.pushes.isEmpty())
    }
}
