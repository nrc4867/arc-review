package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.start.StartColumns
import dev.chieppa.arc.ui.workbench.Workbench

/** The start screen's two columns of cards: the wide main one and the side one. */
@Composable
fun StartColumnsView(workbench: Workbench, columns: StartColumns) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StartSizes.sectionGap), verticalAlignment = Alignment.Top) {
        for ((cards, weight) in listOf(columns.main to StartSizes.MAIN_WEIGHT, columns.side to StartSizes.SIDE_WEIGHT)) {
            Column(Modifier.weight(weight), verticalArrangement = Arrangement.spacedBy(StartSizes.cardGap)) {
                for (card in cards) key(card.id) { StartCardView(workbench, card) }
            }
        }
    }
}
