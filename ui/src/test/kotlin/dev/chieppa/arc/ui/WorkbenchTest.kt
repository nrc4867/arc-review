package dev.chieppa.arc.ui

import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkbenchTest {
    @Test
    fun ctrlKGoesToAnythingWhichOpensTheSwitcher() {
        val workbench = Workbench()
        val action = workbench.keymap.resolve(ArcKeys.GO_TO_ANYTHING, null)
        assertEquals(ArcActionIds.GO_TO_ANYTHING, action?.id)
        action!!.run()
        assertTrue(workbench.switcher.open)
        assertFalse(workbench.paletteOpen)
    }

    @Test
    fun commandsHaveTheirOwnKey() {
        val workbench = Workbench()
        val action = workbench.keymap.resolve(ArcKeys.COMMANDS, null)
        assertEquals(ArcActionIds.COMMANDS, action?.id)
        action!!.run()
        assertTrue(workbench.paletteOpen)
    }

    @Test
    fun noTwoActionsShareAShortcut() {
        assertEquals(emptyList(), Workbench().keymap.conflicts())
    }

    @Test
    fun theDefaultLayoutOpensFilesAndKeepsStagedCommentsClosedUntilItHasContent() {
        val layout = Workbench().frameLayout
        assertEquals(setOf(DrawerIds.FILES), layout.drawers.filter { it.open }.map { it.id }.toSet())
        assertEquals(
            listOf(DrawerIds.FILES, DrawerIds.OUTLINE, DrawerIds.SEARCH, DrawerIds.NOTES, DrawerIds.CONVERSATION, DrawerIds.REVISIONS, DrawerIds.CONSOLE),
            layout.on(Side.Left).map { it.id },
        )
        assertEquals(listOf(DrawerIds.PULL_REQUESTS, DrawerIds.CHECKS, DrawerIds.STAGED_COMMENTS), layout.on(Side.Right).map { it.id })
        assertTrue(layout[DrawerIds.CONSOLE]!!.bottom)
        assertTrue(layout[DrawerIds.STAGED_COMMENTS]!!.bottom)
    }

    @Test
    fun everyDrawerHasAnActionThatTogglesItAndResetBringsTheDefaultBack() {
        val workbench = Workbench()
        for (slot in ArcDrawerSlots.all) {
            val before = workbench.frameLayout[slot.id]!!.open
            workbench.registry[ArcActionIds.toggleDrawer(slot.id)]!!.run()
            assertEquals(!before, workbench.frameLayout[slot.id]!!.open, slot.id)
        }
        workbench.registry[ArcActionIds.RESET_LAYOUT]!!.run()
        assertTrue(workbench.frameLayout[DrawerIds.FILES]!!.open)
        assertFalse(workbench.frameLayout[DrawerIds.OUTLINE]!!.open)
    }

    @Test
    fun quitRunsWhatTheAppGave() {
        var quit = false
        Workbench(onQuit = { quit = true }).registry[ArcActionIds.QUIT]!!.run()
        assertTrue(quit)
    }
}
