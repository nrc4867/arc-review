package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_status
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.stringResource

/** The status bar's start while the Settings page shows: "Settings" and the section it shows. */
@Composable
fun SettingsStatusStart(page: Page.Settings, registry: SettingsRegistry) {
    SettingsShown.of(registry.sections(), page)?.let { StatusItem(stringResource(Res.string.settings_status, it.section.label)) }
}
