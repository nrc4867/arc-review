package dev.chieppa.arc.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** Arc's colors for whatever is inside an [ArcTheme]. */
val LocalArcColors = staticCompositionLocalOf { ArcColorSets.Dark }

/** Arc's design values for the current theme, beside Stretch's. */
object Arc {
    val colors: ArcColors
        @Composable
        @ReadOnlyComposable
        get() = LocalArcColors.current
}
