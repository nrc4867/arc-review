package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** A switcher row's ref, such as `api#412 · auth/token`, in code type, cut short past [ArcSizes.switcherRef]. */
@Composable
fun RowRef(text: String, modifier: Modifier = Modifier, color: Color = Stretch.colors.inkMuted) {
    BasicText(
        text,
        modifier.widthIn(max = ArcSizes.switcherRef),
        style = Stretch.type.value.merge(TextStyle(color = color)),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
