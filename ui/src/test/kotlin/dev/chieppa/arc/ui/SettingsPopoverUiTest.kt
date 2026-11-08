package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.ChangeWash
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsPopoverUiTest {
    @Test
    fun pickingAThemeAndASizeChangesTheLook() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.settingsOpen = true
        waitForIdle()
        onNode(hasContentDescription("Theme") and hasAnyAncestor(isPopup())).performClick()
        onNodeWithText(StretchThemes.WorkshopLight.name).performClick()
        onNode(hasText("125%") and hasAnyAncestor(isPopup())).performClick()
        waitForIdle()
        assertEquals(StretchThemes.WorkshopLight, workbench.preferences.theme)
        assertEquals(1.25f, workbench.preferences.interfaceSize)
    }

    @Test
    fun turningOffWholeLineHighlightMovesChangeWashesToTheGutter() = runComposeUiTest {
        val workbench = Workbench(dispatchers = testDispatchers())
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.settingsOpen = true
        waitForIdle()
        assertEquals(ChangeWash.Line, workbench.preferences.changeWash)
        onNode(hasContentDescription("Highlight whole changed lines") and hasAnyAncestor(isPopup())).performClick()
        waitForIdle()
        assertEquals(ChangeWash.Gutter, workbench.preferences.changeWash)
    }
}
