package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.stretch.theme.Stretch

/** The kinds of text the run views use, as the boards set them. */
enum class RunTextKind { Strong, Body, Small, Mono, MonoSmall, Eyebrow, Heading }

/** One line of [text] in [kind]'s face and [color], cut with an ellipsis. */
@Composable
fun RunText(text: String, kind: RunTextKind, color: Color, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Start, maxLines: Int = 1) {
    val t = Stretch.type
    val style = when (kind) {
        RunTextKind.Strong -> t.body.merge(TextStyle(fontWeight = FontWeight.Medium))
        RunTextKind.Body -> t.body
        RunTextKind.Small -> t.chip.merge(TextStyle(fontWeight = FontWeight.Normal))
        RunTextKind.Mono -> t.value
        RunTextKind.MonoSmall -> t.valueSm
        RunTextKind.Eyebrow -> t.eyebrow
        RunTextKind.Heading -> t.heading
    }
    val shown = if (kind == RunTextKind.Eyebrow) text.uppercase() else text
    BasicText(shown, modifier, style = style.merge(TextStyle(color = color, textAlign = align)), maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}
