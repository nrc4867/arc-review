package dev.chieppa.arc.mcp.dev

/** The development tools' names, as clients call them and the starter's CLAUDE.md lists them. */
object DevToolNames {
    const val LIST = "plugins_list"
    const val TYPE_CHECK = "plugin_type_check"
    const val RELOAD = "plugin_reload"
    const val LOG = "plugin_log"
    const val VIEW_TREE = "plugin_view_tree"
    const val SCREENSHOT = "plugin_view_screenshot"
    const val RUN_ACTION = "plugin_run_action"
    const val FIRE_EVENT = "plugin_fire_event"

    val ALL: List<String> = listOf(LIST, TYPE_CHECK, RELOAD, LOG, VIEW_TREE, SCREENSHOT, RUN_ACTION, FIRE_EVENT)
}
