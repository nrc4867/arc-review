package dev.chieppa.arc.ui

import dev.chieppa.arc.ui.settings.InterfaceSizes
import dev.chieppa.arc.ui.settings.ThemeChoices
import dev.chieppa.stretch.theme.StretchThemes
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsChoicesTest {
    @Test
    fun everyStretchThemeIsOfferedAndWorkshopDarkIsTheDefault() {
        assertEquals(StretchThemes.all, ThemeChoices.all)
        assertEquals(StretchThemes.WorkshopDark, ThemeChoices.byId(null))
        assertEquals(StretchThemes.WorkshopDark, ThemeChoices.byId("no-such-theme"))
        assertEquals(StretchThemes.Ink, ThemeChoices.byId(StretchThemes.Ink.id))
    }

    @Test
    fun aStoredSizeSnapsToTheNearestOffered() {
        assertEquals(1f, InterfaceSizes.nearest(null))
        assertEquals(1.25f, InterfaceSizes.nearest(1.2f))
        assertEquals(1.5f, InterfaceSizes.nearest(3f))
        assertEquals(0.9f, InterfaceSizes.nearest(0.1f))
        assertEquals(125, InterfaceSizes.percent(1.25f))
    }
}
