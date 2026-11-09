package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Tooltip

/** A quiet button in a run's header: its [icon] and [label], or the icon alone with the label as its tooltip when [compact]. */
@Composable
fun RunHeaderButton(label: String, icon: ImageVector, compact: Boolean, onClick: () -> Unit) {
    if (compact) {
        Tooltip(label) { IconButton(icon, label, onClick) }
    } else {
        Button(label, onClick, kind = ButtonKind.Quiet, icon = icon)
    }
}
