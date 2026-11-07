package dev.chieppa.arc.app

import dev.chieppa.arc.store.settings.AppSettings
import dev.chieppa.arc.ui.settings.InterfaceSizes
import dev.chieppa.arc.ui.settings.ThemeChoices
import dev.chieppa.arc.ui.settings.UiPreferences

/** The stored settings as the look to show, falling back to the defaults for anything unknown. */
internal fun AppSettings.toPreferences(): UiPreferences =
    UiPreferences(ThemeChoices.byId(themeId), InterfaceSizes.nearest(interfaceSize), wrapLines ?: false, gutterHighlight ?: false)

/** The look as settings to store, over [base]'s other settings. */
internal fun UiPreferences.toSettings(base: AppSettings = AppSettings()): AppSettings = base.copy(
    themeId = theme.id,
    interfaceSize = interfaceSize,
    wrapLines = wrapLines,
    gutterHighlight = gutterHighlight,
)
