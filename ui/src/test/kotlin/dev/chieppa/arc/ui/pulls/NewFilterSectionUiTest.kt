package dev.chieppa.arc.ui.pulls

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.activity.ActivityChannel
import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.stretch.test.setStretchContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NewFilterSectionUiTest {
    private val watch = PullWatch()
    private val log = ActivityLog()
    private val created = mutableListOf<String>()

    private fun androidx.compose.ui.test.ComposeUiTest.section() {
        setStretchContent { NewFilterSection(watch, log) { created += it } }
        waitForIdle()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.field() = onAllNodes(hasSetTextAction()).onFirst()

    @Test
    fun aPastedSearchLinkMakesAFilterNamedForItsQuery() = runComposeUiTest {
        section()
        field().performTextInput("https://github.com/pulls?q=is%3Aopen+is%3Apr+review-requested%3A%40me")
        onNodeWithText("New filter").performClick()
        waitForIdle()
        val filter = watch.watchlist.filters.single()
        assertEquals("is:open is:pr review-requested:@me", filter.query)
        assertEquals("review-requested:@me", filter.name)
        assertEquals(listOf(filter.id), created)
    }

    @Test
    fun aTypedQueryMakesAFilterWithTheNameGiven() = runComposeUiTest {
        section()
        field().performTextInput("is:pr is:open review-requested:@me org:foo")
        waitForIdle()
        onAllNodes(hasSetTextAction()).onLast().performTextInput("Foo reviews")
        onNodeWithText("New filter").performClick()
        waitForIdle()
        val filter = watch.watchlist.filters.single()
        assertEquals("is:pr is:open review-requested:@me org:foo", filter.query)
        assertEquals("Foo reviews", filter.name)
    }

    @Test
    fun aRefusedLinkSaysWhyOnItsFieldAndInTheConsole() = runComposeUiTest {
        section()
        field().performTextInput("https://github.com/lumen/api/pull/412")
        onNodeWithText("New filter").performClick()
        waitForIdle()
        assertTrue(watch.watchlist.filters.isEmpty())
        val reason = "Not a GitHub search or list of pull requests"
        assertEquals(1, onAllNodesWithContentDescription(reason).fetchSemanticsNodes().size)
        val entry = log.entries(ActivityChannel.GITHUB.id).single()
        assertTrue(entry.failed)
        assertEquals(reason, entry.error)
        assertTrue("https://github.com/lumen/api/pull/412" in entry.title)
    }
}
