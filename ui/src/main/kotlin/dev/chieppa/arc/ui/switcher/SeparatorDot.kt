package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** The small dot between pieces of a line of details. */
@Composable
fun SeparatorDot(modifier: Modifier = Modifier) {
    Box(modifier.size(ArcSizes.separatorDot).background(Stretch.colors.inkSubtle, CircleShape))
}
