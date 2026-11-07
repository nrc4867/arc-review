package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.stretch.theme.Stretch

/** Quiet small text in a switcher row, such as who made it and how many commits it has. */
@Composable
fun RowMeta(text: String, modifier: Modifier = Modifier, color: Color = Stretch.colors.inkSubtle) {
    BasicText(
        text,
        modifier,
        style = Stretch.type.bodySm.merge(TextStyle(color = color, fontSize = Stretch.type.value.fontSize, lineHeight = Stretch.type.value.lineHeight)),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
