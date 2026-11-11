package dev.chieppa.arc.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.host.github.auth.TokenOrigin
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.ui.settings.ArcSettingIds
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.ChangeWash
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SettingsPageUiTest {
    private val pulls = PullWatch(Watchlist(rules = listOf(NotifyRule.Mentions("r1")))).apply {
        viewer = Viewer("nc")
        tokenOrigin = TokenOrigin.Environment("GH_TOKEN")
    }
    private val runs = RunWatch(RunWatchlist())

    private fun ComposeUiTest.open(): Workbench {
        val workbench = Workbench(dispatchers = testDispatchers(), watch = pulls, runWatch = runs)
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.registry[ArcActionIds.SETTINGS]!!.run()
        waitForIdle()
        return workbench
    }

    private fun ComposeUiTest.shows(text: String): Boolean = onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.showsTag(tag: String): Boolean = onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun theSettingsActionOpensOnePageAndShowsItsTabAgain() = runComposeUiTest {
        val workbench = open()
        assertEquals(listOf(ArcKeys.SETTINGS), workbench.registry[ArcActionIds.SETTINGS]!!.keys)
        val page = workbench.state.workspace.pages.single() as Page.Settings
        assertEquals(page.id.value, workbench.state.paneLayout.activeTab)
        assertTrue(shows("Settings · General"))
        workbench.registry[ArcActionIds.SETTINGS]!!.run()
        waitForIdle()
        assertEquals(1, workbench.state.workspace.pages.size)
    }

    @Test
    fun pickingASectionShowsItsCardsAndKeepsItOnThePage() = runComposeUiTest {
        val workbench = open()
        assertTrue(showsTag(ArcSettingIds.WRAP))
        assertFalse(showsTag(ArcSettingIds.THEME))
        onNodeWithText("Appearance").performClick()
        waitForIdle()
        assertTrue(showsTag(ArcSettingIds.THEME))
        assertFalse(showsTag(ArcSettingIds.WRAP))
        assertEquals(ArcSettingIds.APPEARANCE, (workbench.state.workspace.pages.single() as Page.Settings).section)
        assertTrue(shows("Settings · Appearance"))
    }

    @Test
    fun pickingAThemeAndASizeChangesTheLook() = runComposeUiTest {
        val workbench = open()
        onNodeWithText("Appearance").performClick()
        waitForIdle()
        onNode(hasContentDescription("Theme")).performClick()
        onNodeWithText(StretchThemes.WorkshopLight.name).performClick()
        onNodeWithText("125%").performClick()
        waitForIdle()
        assertEquals(StretchThemes.WorkshopLight, workbench.preferences.theme)
        assertEquals(1.25f, workbench.preferences.interfaceSize)
    }

    @Test
    fun turningOffWholeLineHighlightMovesChangeWashesToTheGutter() = runComposeUiTest {
        val workbench = open()
        assertEquals(ChangeWash.Line, workbench.preferences.changeWash)
        onNode(hasContentDescription("Highlight whole changed lines")).performClick()
        waitForIdle()
        assertEquals(ChangeWash.Gutter, workbench.preferences.changeWash)
    }

    @Test
    fun notificationSwitchesChangeTheRulesAndRunNotifications() = runComposeUiTest {
        open()
        onNodeWithText("Notifications").performClick()
        waitForIdle()
        onNode(hasContentDescription("@mentions")).performClick()
        onNode(hasContentDescription("A run of mine succeeded")).performClick()
        waitForIdle()
        assertFalse(pulls.watchlist.rules.single().on)
        assertTrue(RunNotify.MineSucceeded in runs.watchlist.notify)
    }

    @Test
    fun accountsShowsTheSignedInLoginAndWhereItsTokenCameFrom() = runComposeUiTest {
        open()
        onNodeWithText("Accounts").performClick()
        waitForIdle()
        assertTrue(shows("@nc"))
        assertTrue(shows("GH_TOKEN"))
    }

    @Test
    fun searchingShowsMatchingEntriesAcrossSectionsUntilCleared() = runComposeUiTest {
        open()
        val field = onNode(hasSetTextAction())
        field.performTextInput("zoom")
        waitForIdle()
        assertTrue(showsTag(ArcSettingIds.SIZE))
        assertFalse(showsTag(ArcSettingIds.THEME))
        assertFalse(showsTag(ArcSettingIds.WRAP))
        field.performTextClearance()
        field.performTextInput("run")
        waitForIdle()
        assertTrue(showsTag(ArcSettingIds.runNotify(RunNotify.MineFailed)))
        assertFalse(showsTag(ArcSettingIds.SIZE))
        field.performTextClearance()
        waitForIdle()
        assertTrue(showsTag(ArcSettingIds.WRAP))
        assertFalse(showsTag(ArcSettingIds.SIZE))
        assertTrue(onNodeWithTag(ArcSettingIds.PANES_CARD).fetchSemanticsNode().children.isNotEmpty())
    }
}
