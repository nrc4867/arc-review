package dev.chieppa.arc.core.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsSearchTest {
    private val switch = SettingsControl.Switch(true)
    private val theme = SettingsEntry("theme", "Theme", switch, keywords = listOf("dark", "light"))
    private val size = SettingsEntry("size", "Interface size", switch, keywords = listOf("zoom"))
    private val wrap = SettingsEntry("wrap", "Wrap long lines", switch, detail = "Unless a pane says otherwise")
    private val appearance = SettingsOwned("arc", SettingsSection("arc.appearance", "Appearance", cards = listOf(SettingsCard("look", "Look", entries = listOf(theme, size)))))
    private val general = SettingsOwned("arc", SettingsSection("arc.general", "General", cards = listOf(SettingsCard("panes", "Panes", entries = listOf(wrap)))))
    private val sections = listOf(general, appearance)

    @Test
    fun aBlankQueryFindsNothing() {
        assertTrue(SettingsSearch.find(sections, "  ").isEmpty())
    }

    @Test
    fun findsEntriesByLabelAndKeepsOnlyTheMatchingOnes() {
        val found = SettingsSearch.find(sections, "theme")
        assertEquals(listOf("arc.appearance"), found.map { it.owned.section.id })
        assertEquals(listOf(theme), found.single().cards.single().entries)
        assertEquals(listOf(0..4), found.single().matches.getValue("theme"))
    }

    @Test
    fun findsEntriesByKeywordAndDetailWithNothingToMarkInTheLabel() {
        assertEquals(listOf("size"), SettingsSearch.find(sections, "ZOOM").flatMap { f -> f.cards.flatMap { c -> c.entries.map { it.id } } })
        val byDetail = SettingsSearch.find(sections, "otherwise").single()
        assertEquals(emptyList(), byDetail.matches.getValue("wrap"))
    }

    @Test
    fun everyWordMustAppearSomewhere() {
        assertEquals(listOf("size"), SettingsSearch.find(sections, "size appearance").flatMap { f -> f.cards.flatMap { c -> c.entries.map { it.id } } })
        assertTrue(SettingsSearch.find(sections, "size general").isEmpty())
    }

    @Test
    fun rangesMarkEachWordOnceWithoutOverlaps() {
        assertEquals(listOf(0..3, 10..13), SettingsSearch.ranges("Wrap long lines", listOf("wrap", "line")))
        assertEquals(listOf(0..3), SettingsSearch.ranges("Wrap", listOf("wrap", "ra")))
    }

    @Test
    fun mergeOrdersSectionsAndDropsEmptyAndRepeatedOnes() {
        val empty = SettingsSection("plugin.empty", "Empty", order = 0)
        val late = SettingsSection("plugin.late", "Late", order = 900, cards = appearance.section.cards)
        val again = general.section.copy(label = "Again")
        val merged = SettingsMerge.merge(
            listOf(
                SettingsContributor(SettingsContributor.ARC, listOf(appearance.section.copy(order = 2), general.section.copy(order = 1))),
                SettingsContributor("plugin", listOf(empty, late, again)),
            ),
        )
        assertEquals(listOf("arc.general", "arc.appearance", "plugin.late"), merged.map { it.section.id })
        assertEquals("plugin", merged.last().contributor)
        assertEquals("General", merged.first().section.label)
    }

    @Test
    fun aHostedCardKeepsItsSectionAndIsFoundByItsTitleAndKeywords() {
        val plugins = SettingsSection("arc.plugins", "Plugins", cards = listOf(SettingsCard("speckit", "speckit settings", hosted = true, keywords = listOf("tracker"))))
        assertEquals(listOf("arc.plugins"), SettingsMerge.merge(listOf(SettingsContributor("arc", listOf(plugins)))).map { it.section.id })
        val owned = SettingsOwned("arc", plugins)
        assertEquals(listOf("speckit"), SettingsSearch.find(listOf(owned), "tracker").single().cards.map { it.id })
        assertTrue(SettingsSearch.find(listOf(owned), "zoom").isEmpty())
    }
}
