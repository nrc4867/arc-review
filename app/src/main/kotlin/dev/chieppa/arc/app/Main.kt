package dev.chieppa.arc.app

import androidx.compose.ui.window.application
import dev.chieppa.stretch.devtools.DevTools
import dev.chieppa.stretch.document.appDataDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() {
    DevTools.startIfRequested()
    val session = AppSession(appDataDirectory(AppConstants.APP_ID), Conversations.load())
    val initial = session.settings.toPreferences()
    application {
        ArcWindow(
            session = session,
            initial = initial,
            onPreferencesChange = { look -> withContext(Dispatchers.IO) { session.saveSettings { look.toSettings(it) } } },
            onExit = {
                session.close()
                exitApplication()
            },
        )
    }
}
