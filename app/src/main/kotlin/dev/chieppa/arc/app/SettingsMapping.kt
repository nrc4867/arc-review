package dev.chieppa.arc.app

import dev.chieppa.arc.store.settings.AppSettings
import dev.chieppa.arc.ui.settings.InterfaceSizes
import dev.chieppa.arc.ui.settings.ThemeChoices
import dev.chieppa.arc.ui.settings.UiPreferences

/** The stored settings as the look to show, falling back to the defaults for anything unknown. */
internal fun AppSettings.toPreferences(): UiPreferences =
    UiPreferences(ThemeChoices.byId(themeId), InterfaceSizes.nearest(interfaceSize))

/** The look as settings to store. */
internal fun UiPreferences.toSettings(): AppSettings = AppSettings(theme.id, interfaceSize)
