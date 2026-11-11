package dev.chieppa.arc.plugins.api

/** The icon names plugins may give actions, panels, panes, rows and buttons; Arc draws each with its own or Stretch's icon. */
object PluginIcons {
    const val PLUGIN = "plugin"

    val NAMES: List<String> = listOf(
        "branch", "check", "close", "code", "comment", "commit", "console", "file", "filter", "folder", "info", "link",
        "note", "play", "plugin", "plus", "pull-request", "refresh", "runs", "search", "settings", "spec", "stop",
        "tag", "warning",
    )
}
