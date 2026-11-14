package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.ui.conversation.ThreadJumps
import dev.chieppa.arc.ui.workbench.Workbench
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class OwnAuthorUiTest {
    private val data = FakeReviewData()
    private val host = FakeConversations()
    private val picture = "https://avatars.githubusercontent.com/u/1?v=4"
    private val me = Viewer("nchieppa", name = "Nicholas Chieppa", avatarUrl = picture)

    private fun png(): ByteArray = ByteArrayOutputStream().also { ImageIO.write(BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB), "png", it) }.toByteArray()

    private fun Workbench.note(text: String): String {
        val controller = comments.controller
        val id = controller.startComment(this@OwnAuthorUiTest.data.source, CommentAnchor("app/Main.kt", 41))
        controller.write(id, text)
        controller.keepAsNote(id)
        return id
    }

    @Test
    fun anOwnNoteShowsTheViewersPictureBesideYou() = runComposeUiTest {
        host.avatars = mapOf(picture to png())
        val workbench = openReview(data, conversations = host)
        workbench.watch.viewer = me
        val id = workbench.note("Check the backfill")
        waitForIdle()
        assertTrue(ThreadJumps.toLocal(workbench, id))
        waitForIdle()
        assertTrue(shows("Check the backfill"))
        assertTrue(shows("You"))
        assertTrue(picture in host.avatarsAsked)
        assertTrue(onAllNodesWithContentDescription(me.name!!).fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun anOwnStagedCommentWithNoOneSignedInShowsYouWithoutAPicture() = runComposeUiTest {
        val workbench = openReview(data, conversations = host)
        val controller = workbench.comments.controller
        val id = controller.startComment(data.source, CommentAnchor("app/Main.kt", 41)).also { controller.write(it, "Ship it"); controller.stage(it) }
        waitForIdle()
        assertTrue(ThreadJumps.toLocal(workbench, id))
        waitForIdle()
        assertTrue(shows("Ship it"))
        assertTrue(shows("You"))
        assertEquals(emptyList(), host.avatarsAsked)
    }
}
