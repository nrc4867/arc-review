package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.chieppa.arc.ui.conversation.Initials
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** The type size of the initials in a small avatar, as the boards draw it. */
private const val INITIALS_SP = 8.5f

/** A small round avatar with [name]'s initials. */
@Composable
fun SmallAvatar(name: String, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    Box(modifier.size(ArcSizes.smallAvatar).background(c.surfaceControlHi, CircleShape), contentAlignment = Alignment.Center) {
        BasicText(Initials.of(name), style = Stretch.type.chip.merge(TextStyle(color = c.ink, fontSize = INITIALS_SP.sp, lineHeight = INITIALS_SP.sp, fontWeight = FontWeight.SemiBold)))
    }
}
