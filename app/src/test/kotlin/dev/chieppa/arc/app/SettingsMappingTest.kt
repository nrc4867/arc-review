package dev.chieppa.arc.app

import dev.chieppa.arc.store.settings.AppSettings
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsMappingTest {
    @Test
    fun nothingStoredOpensInWorkshopDarkAtNormalSize() {
        assertEquals(UiPreferences(StretchThemes.WorkshopDark, 1f), AppSettings().toPreferences())
    }

    @Test
    fun theLookSurvivesARoundTrip() {
        val look = UiPreferences(StretchThemes.InkDark, 1.25f)
        assertEquals(look, look.toSettings().toPreferences())
        assertEquals(AppSettings("ink-dark", 1.25f), look.toSettings())
    }
}
