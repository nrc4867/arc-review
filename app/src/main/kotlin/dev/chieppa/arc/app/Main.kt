package dev.chieppa.arc.app

import androidx.compose.ui.window.application
import dev.chieppa.arc.store.settings.SettingsFile
import dev.chieppa.stretch.devtools.DevTools
import dev.chieppa.stretch.document.appDataDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() {
    DevTools.startIfRequested()
    val settings = SettingsFile(appDataDirectory(AppConstants.APP_ID).resolve(AppConstants.SETTINGS_FILE))
    val initial = settings.load().toPreferences()
    application {
        ArcWindow(
            initial = initial,
            onPreferencesChange = { withContext(Dispatchers.IO) { settings.save(it.toSettings()) } },
            onExit = ::exitApplication,
        )
    }
}
