package dev.chieppa.arc.ui.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_go_to_anything
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Kbd
import org.jetbrains.compose.resources.stringResource

/** The top bar's "Go to anything" field: a search field look with its key, which opens the palette. */
@Composable
fun GoToAnythingField(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    Row(
        modifier
            .width(ArcSizes.goToAnything)
            .height(StretchSizes.seg)
            .clip(shape)
            .background(c.surfaceWell)
            .border(Stretch.stroke, c.lineControl, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StretchIcons.Search, c.inkMuted)
        BasicText(stringResource(Res.string.action_go_to_anything), Modifier.weight(1f), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1)
        Kbd(ArcKeys.GO_TO_ANYTHING.caps())
    }
}
