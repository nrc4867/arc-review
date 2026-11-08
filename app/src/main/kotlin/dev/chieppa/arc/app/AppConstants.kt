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

    /** The sources opened lately, which the start screen lists. */
    const val RECENT_FILE = "recent.json"
    const val CLONES_FOLDER = "clones"

    /** The person's drafts, staged comments, notes and summaries. */
    const val CONVERSATION_FILE = "conversation.json"

    /** People's pictures from GitHub, kept for a day. */
    const val AVATARS_FOLDER = "avatars"

    /** How long the workspace waits after a change before it is saved. */
    const val SAVE_DELAY_MILLIS = 400L
    val WINDOW_WIDTH = 1440.dp
    val WINDOW_HEIGHT = 900.dp
}
