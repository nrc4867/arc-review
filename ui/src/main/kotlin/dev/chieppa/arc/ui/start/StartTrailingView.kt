package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartTone
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.ui.conversation.StateChip
import dev.chieppa.arc.ui.conversation.StateLook
import dev.chieppa.arc.ui.conversation.ageText
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.ControlLabel
import dev.chieppa.stretch.ui.Kbd

/** The item at the end of a start row: a count, a chip in its tone, an age, or key caps. */
@Composable
fun StartTrailingView(workbench: Workbench, trailing: StartTrailing) {
    val c = Stretch.colors
    when (trailing) {
        is StartTrailing.Count -> ControlLabel(trailing.count.toString(), Stretch.type.valueSm, c.inkMuted)
        is StartTrailing.Chip -> when (trailing.tone) {
            StartTone.Plain -> Chip(trailing.text)
            StartTone.Accent -> Chip(trailing.text, kind = ChipKind.On)
            StartTone.Warning -> Chip(trailing.text, kind = ChipKind.Warn)
            StartTone.Error -> StateChip(trailing.text, null, StateLook(c.errorWash, c.errorEdge, c.errorInk))
        }
        is StartTrailing.Time -> ControlLabel(ageText(trailing.epochSeconds), Stretch.type.bodySm, c.inkSubtle)
        is StartTrailing.Keys -> ActionKeys(workbench, trailing.action)
        is StartTrailing.Caps -> Kbd(trailing.caps)
    }
}
