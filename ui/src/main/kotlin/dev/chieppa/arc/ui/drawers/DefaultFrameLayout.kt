package dev.chieppa.arc.ui.drawers

import dev.chieppa.stretch.frame.DrawerPlacement
import dev.chieppa.stretch.frame.FrameLayout

/** Every drawer where its [DrawerSlot] puts it, open or closed as the board draws it. */
fun defaultFrameLayout(): FrameLayout = FrameLayout(
    ArcDrawerSlots.all.map { slot ->
        DrawerPlacement(slot.id, slot.side, width = slot.width, open = slot.openByDefault, bottom = slot.bottom)
    },
)
