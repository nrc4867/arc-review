package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_binary
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A binary file's pane: a file icon and its kind, centred on the pane's surface. */
@Composable
fun BinaryBody(modifier: Modifier = Modifier) {
    val c = Stretch.colors
    Column(
        modifier.background(c.surfacePanel),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(StretchIcons.File, c.inkSubtle, size = ArcSizes.binaryIcon)
        BasicText(stringResource(Res.string.pane_binary), style = Stretch.type.body.merge(TextStyle(color = c.inkMuted)))
    }
}
