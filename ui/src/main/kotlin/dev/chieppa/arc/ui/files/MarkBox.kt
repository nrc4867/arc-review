package dev.chieppa.arc.ui.files

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons

/**
 * A file's or folder's reviewed mark at the end of its tree row: empty, checked, mixed (null) for a folder whose files
 * differ, or, with [changedSince], the changed-since mark in the warning color. Clicking it toggles.
 */
@Composable
fun MarkBox(checked: Boolean?, onToggle: (Boolean) -> Unit, description: String, modifier: Modifier = Modifier, changedSince: Boolean = false) {
    val c = Stretch.colors
    val shape = Stretch.shapes.segShape
    val (fill, edge, ink) = when {
        changedSince -> Triple(c.warnWash, c.warnEdge, c.warnInk)
        checked != false -> Triple(c.accent, c.accent, c.onAccent)
        else -> Triple(Color.Transparent, c.lineStrong, c.ink)
    }
    Box(
        modifier
            .size(ArcSizes.markBox)
            .clip(shape)
            .background(fill)
            .border(Stretch.stroke, edge, shape)
            .triStateToggleable(
                state = when (checked) {
                    true -> ToggleableState.On
                    false -> ToggleableState.Off
                    null -> ToggleableState.Indeterminate
                },
                role = Role.Checkbox,
                onClick = { onToggle(checked != true || changedSince) },
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        when {
            changedSince -> Icon(ArcIcons.ChangedSince, ink, size = ArcSizes.markBoxIcon)
            checked == true -> Icon(StretchIcons.Check, ink, size = ArcSizes.markBoxIcon)
            checked == null -> Icon(StretchIcons.Minus, ink, size = ArcSizes.markBoxIcon)
        }
    }
}
