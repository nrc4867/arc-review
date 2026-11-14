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

    /** The run filters, the Mine rules and what is watched. */
    const val RUNS_FILE = "runs.json"

    /** Each job's step timings over its last successful runs. */
    const val TIMINGS_FILE = "timings.json"

    /** The kept runs: each run's jobs and the kept part of their logs. */
    const val RUNS_FOLDER = "runs"

    /** Downloaded artifacts, at most 2 GB. */
    const val ARTIFACTS_FOLDER = "artifacts"

    /** People's pictures from GitHub, kept for a day. */
    const val AVATARS_FOLDER = "avatars"

    /** Who the person signed in to GitHub is, by their token's fingerprint, kept for a day. */
    const val VIEWER_FILE = "viewer.json"

    /** The plugins' sources, which are enabled and their settings. */
    const val PLUGINS_FILE = "plugins.json"

    /** Each plugin's private store, as `<id>.json`. */
    const val PLUGIN_STORE_FOLDER = "plugin-store"

    /** Arc's own clones of git plugin sources, one folder per url. */
    const val PLUGIN_GIT_FOLDER = "plugin-git"

    /** The plugin host the plugins' Node process runs, as `host-<hash>/`, and the folder it runs in, `work`. */
    const val PLUGIN_RUNTIME_FOLDER = "plugin-runtime"

    /** The neutral folder the plugins' Node process runs in, inside [PLUGIN_RUNTIME_FOLDER]. */
    const val PLUGIN_WORK_FOLDER = "work"

    /** The pinned Node, downloaded the first time a plugin is enabled. */
    const val RUNTIMES_FOLDER = "runtimes"

    /** How often Arc asks GitHub for a newer release while the window is open. */
    const val UPDATE_CHECK_MILLIS = 24 * 60 * 60 * 1000L

    /** How long the workspace waits after a change before it is saved. */
    const val SAVE_DELAY_MILLIS = 400L
    val WINDOW_WIDTH = 1440.dp
    val WINDOW_HEIGHT = 900.dp
    val INSTALL_WIDTH = 480.dp
    val INSTALL_HEIGHT = 320.dp
}
