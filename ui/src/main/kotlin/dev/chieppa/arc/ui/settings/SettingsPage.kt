package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.settings.SettingsSearch
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ScrollColumn

/**
 * The Settings page: the rail of every contributor's sections beside the cards of the section the page shows, on the
 * canvas. Typing in the rail's search shows the matching entries across all sections instead; clearing it, or picking
 * a section, shows that section again.
 */
@Composable
fun SettingsPage(page: Page.Settings, workbench: Workbench, modifier: Modifier = Modifier) {
    val registry = workbench.settings
    val sections = registry.sections()
    var query by rememberSaveable(page.id.value) { mutableStateOf("") }
    val shown = SettingsShown.of(sections, page)
    Row(modifier.fillMaxSize()) {
        SettingsNav(sections, shown?.section?.id, query, { query = it }) { id ->
            query = ""
            workbench.panes.showSettings(page, id)
        }
        Box(Modifier.weight(1f).fillMaxHeight().background(Stretch.colors.surfaceCanvas)) {
            ScrollColumn(Modifier.fillMaxSize().padding(horizontal = StretchSpacing.space28, vertical = StretchSpacing.space20)) {
                if (SettingsSearch.words(query).isNotEmpty()) {
                    SettingsSearchResults(SettingsSearch.find(sections, query), registry::change) { contributor, card -> registry.Hosted(contributor, card) }
                } else if (shown != null) {
                    SettingsCardsView(shown.section.cards, emptyMap(), { entry, value -> registry.change(shown.contributor, entry, value) }) {
                        registry.Hosted(shown.contributor, it)
                    }
                }
            }
        }
    }
}
