package dev.chieppa.arc.ui.menus

import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.menu_file
import dev.chieppa.arc.design.generated.menu_go
import dev.chieppa.arc.design.generated.menu_view
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots

/** The menus, holding the actions Arc has so far. */
object ArcMenus {
    val all: List<ArcMenu> = listOf(
        ArcMenu(Res.string.menu_file, listOf(ArcActionIds.SETTINGS, null, ArcActionIds.QUIT)),
        ArcMenu(
            Res.string.menu_view,
            ArcDrawerSlots.all.map { ArcActionIds.toggleDrawer(it.id) } + null + ArcActionIds.RESET_LAYOUT,
        ),
        ArcMenu(Res.string.menu_go, listOf(ArcActionIds.GO_TO_ANYTHING)),
    )
}
