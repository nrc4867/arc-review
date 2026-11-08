package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.menus.TargetActionItem
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuDivider

/**
 * A start card's menu: Hide, Move up, Move down and Move to the other column, each where it applies, then the card's own
 * items, then Restore while the person has changed this screen.
 */
@Composable
fun StartCardMenu(workbench: Workbench, card: StartCard, close: () -> Unit) {
    for (id in listOf(ArcActionIds.HIDE_START_CARD, ArcActionIds.MOVE_START_CARD_UP, ArcActionIds.MOVE_START_CARD_DOWN, ArcActionIds.MOVE_START_CARD_ACROSS)) {
        TargetActionItem(workbench, id, MenuTarget.OfStart(ArcStartActions.onCard(id, card.id)), close)
    }
    StartMenuItems(workbench, card.menu, close)
    if (!workbench.start.screen.isDefault) {
        MenuDivider()
        TargetActionItem(workbench, ArcActionIds.RESTORE_START_CARDS, MenuTarget.OfStart(ArcStartActions.of(ArcActionIds.RESTORE_START_CARDS)), close)
    }
}
