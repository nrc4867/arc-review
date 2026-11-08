package dev.chieppa.arc.ui.menus

import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.menu_file
import dev.chieppa.arc.design.generated.menu_go
import dev.chieppa.arc.design.generated.menu_review
import dev.chieppa.arc.design.generated.menu_view
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots

/** The menus, holding the actions Arc has so far. */
object ArcMenus {
    val all: List<ArcMenu> = listOf(
        ArcMenu(Res.string.menu_file, listOf(ArcActionIds.ADD_SOURCE, ArcActionIds.REMOVE_SOURCE, ArcActionIds.OPEN_FILE, null, ArcActionIds.SETTINGS, null, ArcActionIds.QUIT)),
        ArcMenu(
            Res.string.menu_view,
            ArcDrawerSlots.all.map { ArcActionIds.toggleDrawer(it.id) } + null +
                listOf(ArcActionIds.TOGGLE_FULL_FILE, ArcActionIds.TOGGLE_SPLIT, ArcActionIds.TOGGLE_WRAP, ArcActionIds.TOGGLE_PIN, ArcActionIds.CLOSE_PANE, null, ArcActionIds.RESET_LAYOUT),
        ),
        ArcMenu(
            Res.string.menu_review,
            listOf(ArcActionIds.MARK_AND_ADVANCE, ArcActionIds.TOGGLE_REVIEWED, ArcActionIds.SET_ASIDE, null, ArcActionIds.COMMENT_HERE, ArcActionIds.PUSH_REVIEW),
        ),
        ArcMenu(
            Res.string.menu_go,
            listOf(
                ArcActionIds.GO_TO_ANYTHING, ArcActionIds.OPEN_FILE, ArcActionIds.FIND_IN_FILE, null,
                ArcActionIds.NEXT_FILE, ArcActionIds.PREVIOUS_FILE, ArcActionIds.NEXT_CHANGE, ArcActionIds.PREVIOUS_CHANGE,
            ),
        ),
    )
}
