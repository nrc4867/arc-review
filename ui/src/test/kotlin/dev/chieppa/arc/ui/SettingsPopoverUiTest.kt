package dev.chieppa.arc.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SettingsPopoverUiTest {
    @Test
    fun pickingAThemeAndASizeChangesTheLook() = runComposeUiTest {
        val workbench = Workbench()
        setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        workbench.settingsOpen = true
        waitForIdle()
        onNodeWithContentDescription("Theme").performClick()
        onNodeWithText(StretchThemes.WorkshopLight.name).performClick()
        onNodeWithText("125%").performClick()
        waitForIdle()
        assertEquals(StretchThemes.WorkshopLight, workbench.preferences.theme)
        assertEquals(1.25f, workbench.preferences.interfaceSize)
    }
}
