package dev.chieppa.arc.ui.files

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** The letter for how a file changed: A in the added color, D in the removed color, the rest muted. */
@Composable
fun StatusLetter(status: ChangeStatus, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val color = when (status) {
        ChangeStatus.Added -> c.diffAddInk
        ChangeStatus.Deleted -> c.diffDelInk
        ChangeStatus.Modified -> c.warnInk
        else -> c.inkMuted
    }
    BasicText(
        status.letter.toString(),
        modifier.width(ArcSizes.statusLetter),
        style = Stretch.type.valueSm.merge(TextStyle(color = color, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)),
    )
}
