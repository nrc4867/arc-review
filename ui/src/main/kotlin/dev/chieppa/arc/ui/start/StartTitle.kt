package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** The start screen's title for the selected sources, after its [icon]. */
@Composable
fun StartTitle(icon: ImageVector, text: String) {
    val c = Stretch.colors
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, c.inkControl, size = StartSizes.headerIcon)
        BasicText(
            text,
            style = Stretch.type.heading.copy(color = c.inkStrong, fontSize = StartSizes.TITLE_SP.sp, lineHeight = StartSizes.TITLE_LINE_SP.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
