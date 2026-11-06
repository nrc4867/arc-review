package dev.chieppa.arc.ui.drawers

import androidx.compose.runtime.Composable
import dev.chieppa.stretch.frame.DrawerSpec
import org.jetbrains.compose.resources.stringResource

/** The frame's drawers, one per [DrawerSlot]. None has content yet, so each opens plain. */
@Composable
fun arcDrawerSpecs(): List<DrawerSpec> = ArcDrawerSlots.all.map { slot ->
    DrawerSpec(slot.id, stringResource(slot.title), slot.icon) {}
}
