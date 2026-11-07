package dev.chieppa.arc.app

import androidx.compose.ui.unit.dp

/** Where the app keeps its files and how its window first opens. */
internal object AppConstants {
    const val APP_ID = "dev.chieppa.arc"
    const val SETTINGS_FILE = "settings.properties"
    const val WORKSPACE_FILE = "workspace.json"

    /** The saved reviews a workspace starts from when there is no workspace file. */
    const val REVIEWS_FOLDER = "reviews"
    const val REPOS_FILE = "repos.json"
    const val WATCHLIST_FILE = "filters.json"
    const val CLONES_FOLDER = "clones"

    /** How long the workspace waits after a change before it is saved. */
    const val SAVE_DELAY_MILLIS = 400L
    val WINDOW_WIDTH = 1440.dp
    val WINDOW_HEIGHT = 900.dp
}
