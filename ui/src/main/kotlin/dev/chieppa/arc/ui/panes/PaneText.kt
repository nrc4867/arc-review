package dev.chieppa.arc.ui.panes

import androidx.compose.ui.graphics.ImageBitmap
import dev.chieppa.stretch.diff.FileDiff

/**
 * A pane's file as text: [old] at the pane's base and [new] at its revision, each null where the file is not, and
 * [diff] from one to the other when the pane has a base. A [binary] file has no text on either side, and is
 * [binaryChanged] when its bytes differ between the two. A picture, binary or SVG, also comes drawn as [oldImage] and
 * [newImage]; a picture the same on both sides comes as [newImage] alone.
 */
data class PaneText(
    val old: String?,
    val new: String?,
    val diff: FileDiff? = null,
    val binary: Boolean = false,
    val binaryChanged: Boolean = false,
    val oldImage: ImageBitmap? = null,
    val newImage: ImageBitmap? = null,
) {
    /** Whether the file is a picture on either side. */
    val image: Boolean get() = oldImage != null || newImage != null
}
