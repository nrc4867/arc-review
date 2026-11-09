package dev.chieppa.arc.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Tooltip

/** An icon-only button whose [label] shows in a tooltip on hover, with its [keys] when it has them; [on] while what it switches is on. */
@Composable
fun TipIconButton(icon: ImageVector, label: String, onClick: () -> Unit, size: Dp = StretchSizes.seg, keys: List<String>? = null, on: Boolean = false) {
    Tooltip(label, keys) { IconButton(icon, label, onClick, size = size, on = on) }
}
