package dev.chieppa.arc.ui.actions

import dev.chieppa.stretch.core.ActionId

/** The ids of Arc's actions. An action's text key and icon key are its id. */
object ArcActionIds {
    val GO_TO_ANYTHING = ActionId("go-to-anything")
    val COMMANDS = ActionId("commands")
    val SETTINGS = ActionId("settings")
    val RESET_LAYOUT = ActionId("reset-layout")
    val QUIT = ActionId("quit")

    val ADD_SOURCE = ActionId("add-source")
    val NEXT_FILE = ActionId("next-file")
    val PREVIOUS_FILE = ActionId("previous-file")
    val NEXT_CHANGE = ActionId("next-change")
    val PREVIOUS_CHANGE = ActionId("previous-change")
    val MARK_AND_ADVANCE = ActionId("mark-and-advance")
    val TOGGLE_REVIEWED = ActionId("toggle-reviewed")
    val SET_ASIDE = ActionId("set-aside")
    val FIND_IN_FILE = ActionId("find-in-file")
    val OPEN_FILE = ActionId("open-file")
    val TOGGLE_PIN = ActionId("toggle-pin")
    val TOGGLE_FULL_FILE = ActionId("toggle-full-file")
    val TOGGLE_SPLIT = ActionId("toggle-split")
    val CLOSE_PANE = ActionId("close-pane")
    val REMOVE_SOURCE = ActionId("remove-source")
    val ACTIVATE_SOURCE = ActionId("activate-source")
    val COPY_SOURCE_REF = ActionId("copy-source-ref")
    val OPEN_CHANGE = ActionId("open-change")
    val OPEN_IN_NEW_TAB = ActionId("open-in-new-tab")
    val OPEN_TO_SIDE = ActionId("open-to-side")
    val OPEN_IN_NEW_WINDOW = ActionId("open-in-new-window")
    val OPEN_PINNED = ActionId("open-pinned")
    val OPEN_FULL_FILE = ActionId("open-full-file")
    val RESTORE = ActionId("restore")
    val COPY_PATH = ActionId("copy-path")
    val COPY_FILE_REF = ActionId("copy-file-ref")
    val EXPAND_ALL = ActionId("expand-all")
    val COLLAPSE_ALL = ActionId("collapse-all")
    val TOGGLE_WRAP = ActionId("toggle-wrap")
    val OPEN_REVISIONS = ActionId("open-revisions")

    private const val DRAWER_PREFIX = "drawer-"

    /** The action that opens and closes the drawer [drawerId]. */
    fun toggleDrawer(drawerId: String) = ActionId(DRAWER_PREFIX + drawerId)

    /** The drawer [id] toggles, when it is a drawer action. */
    fun drawerOf(id: String): String? = id.takeIf { it.startsWith(DRAWER_PREFIX) }?.removePrefix(DRAWER_PREFIX)
}
