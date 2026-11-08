package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chieppa.stretch.theme.StretchSpacing

/** The row of actions at the foot of a thread card, on [fill]. */
@Composable
fun CardFooter(fill: Color, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(fill).padding(StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
