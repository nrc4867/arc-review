package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** One fact in the push confirmation: its icon and [label], the [value] in strong ink, and a quieter [detail]. */
@Composable
fun PushFactRow(icon: ImageVector, label: String, value: String, detail: String? = null) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = ArcSizes.pushFactRow),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, c.inkMuted, size = ArcSizes.pullIcon)
        BasicText(label, Modifier.width(ArcSizes.pushFactLabel), style = Stretch.type.body.merge(TextStyle(color = c.inkControl)), maxLines = 1)
        BasicText(value, style = Stretch.type.body.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)), maxLines = 1)
        detail?.let { BasicText(it, Modifier.weight(1f), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}
