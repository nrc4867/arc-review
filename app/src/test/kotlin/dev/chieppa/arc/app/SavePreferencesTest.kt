package dev.chieppa.arc.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavePreferencesTest {
    @Test
    fun eachChangeToTheLookIsSavedAndOpeningIsNot() = runComposeUiTest {
        val workbench = Workbench()
        val saved = mutableListOf<UiPreferences>()
        setContent { SavePreferences(workbench) { saved += it } }
        waitForIdle()
        assertEquals(emptyList(), saved)
        val light = UiPreferences(StretchThemes.WorkshopLight, 1.25f)
        workbench.preferences = light
        waitForIdle()
        assertEquals(listOf(light), saved)
    }
}
