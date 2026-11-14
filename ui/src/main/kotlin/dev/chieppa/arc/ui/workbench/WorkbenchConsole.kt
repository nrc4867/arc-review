package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.ui.activity.ActivityFilter
import dev.chieppa.arc.ui.drawers.DrawerIds

/** Opens the Console drawer, showing only failures when [errorsOnly]. */
fun Workbench.openConsole(errorsOnly: Boolean = false) {
    if (errorsOnly) console.filter = ActivityFilter.Errors
    if (frameLayout.drawers.firstOrNull { it.id == DrawerIds.CONSOLE }?.open != true) toggleDrawer(DrawerIds.CONSOLE)
}
