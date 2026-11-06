package dev.chieppa.arc.ui.finder

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Places a popup centred across the window, [top] pixels down, as the palette sits. */
class TopCenter(private val top: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val x = ((windowSize.width - popupContentSize.width) / 2).coerceAtLeast(0)
        return IntOffset(x, top.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)))
    }
}
