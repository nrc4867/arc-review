package dev.chieppa.arc.ui.drawers

import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.drawer_checks
import dev.chieppa.arc.design.generated.drawer_console
import dev.chieppa.arc.design.generated.drawer_conversation
import dev.chieppa.arc.design.generated.drawer_files
import dev.chieppa.arc.design.generated.drawer_notes
import dev.chieppa.arc.design.generated.drawer_outline
import dev.chieppa.arc.design.generated.drawer_pull_requests
import dev.chieppa.arc.design.generated.drawer_revisions
import dev.chieppa.arc.design.generated.drawer_search
import dev.chieppa.arc.design.generated.drawer_staged_comments
import dev.chieppa.stretch.frame.Side
import dev.chieppa.stretch.theme.StretchIcons

/** Arc's drawers in rail order, as the main window's board places them. */
object ArcDrawerSlots {
    val all: List<DrawerSlot> = listOf(
        DrawerSlot(DrawerIds.FILES, Res.string.drawer_files, StretchIcons.Folder, Side.Left, openByDefault = true),
        DrawerSlot(DrawerIds.OUTLINE, Res.string.drawer_outline, ArcIcons.Outline, Side.Left),
        DrawerSlot(DrawerIds.SEARCH, Res.string.drawer_search, StretchIcons.Search, Side.Left),
        DrawerSlot(DrawerIds.NOTES, Res.string.drawer_notes, ArcIcons.Note, Side.Left),
        DrawerSlot(DrawerIds.CONVERSATION, Res.string.drawer_conversation, ArcIcons.Comment, Side.Left),
        DrawerSlot(DrawerIds.REVISIONS, Res.string.drawer_revisions, ArcIcons.Matrix, Side.Left),
        DrawerSlot(DrawerIds.CONSOLE, Res.string.drawer_console, StretchIcons.Command, Side.Left, bottom = true),
        DrawerSlot(DrawerIds.PULL_REQUESTS, Res.string.drawer_pull_requests, ArcIcons.Filter, Side.Right),
        DrawerSlot(DrawerIds.CHECKS, Res.string.drawer_checks, ArcIcons.CheckRun, Side.Right),
        DrawerSlot(DrawerIds.STAGED_COMMENTS, Res.string.drawer_staged_comments, ArcIcons.Stage, Side.Right, bottom = true),
    )

    fun byId(id: String): DrawerSlot? = all.firstOrNull { it.id == id }
}
