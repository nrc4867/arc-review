package dev.chieppa.arc.design

import androidx.compose.ui.graphics.Color

/** [ArcColors] for dark and light Stretch themes. */
object ArcColorSets {
    val Dark = ArcColors(
        guest = Color(0xFFA69CF0),
        guestWash = Color(0xFF22213A),
        guestEdge = Color(0xFF574F9C),
        addressed = Color(0xFF7FB6F2),
        addressedWash = Color(0xFF172434),
        addressedEdge = Color(0xFF31608F),
        passFill = Color(0xFF2F5A36),
        passEdge = Color(0xFF4F8F5B),
    )

    val Light = ArcColors(
        guest = Color(0xFF5B4FC4),
        guestWash = Color(0xFFEFEDFC),
        guestEdge = Color(0xFFB3ACEC),
        addressed = Color(0xFF2B66A6),
        addressedWash = Color(0xFFE7F0FA),
        addressedEdge = Color(0xFF9CC0E8),
        passFill = Color(0xFFDCEFDF),
        passEdge = Color(0xFF4F8F5B),
    )

    fun forDark(dark: Boolean): ArcColors = if (dark) Dark else Light
}
