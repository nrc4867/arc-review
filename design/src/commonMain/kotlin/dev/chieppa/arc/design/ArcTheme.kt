package dev.chieppa.arc.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.chieppa.stretch.theme.LocalStretchTheme

/** Provides [ArcColors] to match the Stretch theme around [content], dark or light. */
@Composable
fun ArcTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalArcColors provides ArcColorSets.forDark(LocalStretchTheme.current.isDark), content = content)
}
