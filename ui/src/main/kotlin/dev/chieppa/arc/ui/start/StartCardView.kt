package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.chieppa.arc.core.start.StartCard
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.ui.Card
import dev.chieppa.stretch.ui.Divider

/**
 * One start card: its header, then Arc's own composable for a slot card or else its rows, then its footer links. A
 * right-click opens the card's menu. The card is tagged with its id.
 */
@Composable
fun StartCardView(workbench: Workbench, card: StartCard) {
    ContextMenuArea({ close -> StartCardMenu(workbench, card, close) }) {
        Card(Modifier.fillMaxWidth().testTag(card.id)) {
            StartCardHeader(workbench, card)
            Divider()
            when (card.id) {
                StartCards.SETTINGS -> SettingsRows(workbench)
                StartCards.PROGRESS -> ProgressTable(workbench)
            }
            card.rows.forEachIndexed { i, row ->
                if (i > 0) Divider()
                StartRowView(workbench, row)
            }
            val links = card.links.filter { StartRunner.available(workbench, it.action) }
            if (links.isNotEmpty()) {
                Divider()
                StartFooter(workbench, links)
            }
        }
    }
}
