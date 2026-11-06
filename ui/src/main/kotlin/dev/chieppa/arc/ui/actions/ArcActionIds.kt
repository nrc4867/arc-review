package dev.chieppa.arc.ui.actions

import dev.chieppa.stretch.core.ActionId

/** The ids of Arc's actions. An action's text key and icon key are its id. */
object ArcActionIds {
    val GO_TO_ANYTHING = ActionId("go-to-anything")
    val SETTINGS = ActionId("settings")
    val RESET_LAYOUT = ActionId("reset-layout")
    val QUIT = ActionId("quit")

    private const val DRAWER_PREFIX = "drawer-"

    /** The action that opens and closes the drawer [drawerId]. */
    fun toggleDrawer(drawerId: String) = ActionId(DRAWER_PREFIX + drawerId)

    /** The drawer [id] toggles, when it is a drawer action. */
    fun drawerOf(id: String): String? = id.takeIf { it.startsWith(DRAWER_PREFIX) }?.removePrefix(DRAWER_PREFIX)
}
