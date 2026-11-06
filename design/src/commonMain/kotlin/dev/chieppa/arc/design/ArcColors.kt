package dev.chieppa.arc.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Arc's own colors, beside Stretch's tokens and never redefining one. The guest colors mark what an outside agent
 * wrote; the addressed colors mark a thread that is still open but whose lines changed in a later revision.
 */
@Immutable
data class ArcColors(
    val guest: Color,
    val guestWash: Color,
    val guestEdge: Color,
    val addressed: Color,
    val addressedWash: Color,
    val addressedEdge: Color,
)
