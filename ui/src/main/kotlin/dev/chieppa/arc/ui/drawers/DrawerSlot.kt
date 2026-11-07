package dev.chieppa.arc.ui.drawers

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.stretch.frame.FrameLayout
import dev.chieppa.stretch.frame.Side
import org.jetbrains.compose.resources.StringResource

/** One of Arc's drawers: its rail icon and title, and where it sits, and how wide, until the person moves it. */
data class DrawerSlot(
    val id: String,
    val title: StringResource,
    val icon: ImageVector,
    val side: Side,
    val bottom: Boolean = false,
    val openByDefault: Boolean = false,
    val width: Float = FrameLayout.DEFAULT_WIDTH,
)
