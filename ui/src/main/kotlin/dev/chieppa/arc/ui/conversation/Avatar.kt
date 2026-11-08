package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import dev.chieppa.arc.design.Arc
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/**
 * A round avatar for [name]: their picture from [url] once [images] has it, their initials until then, or the guest
 * mark for an outside agent.
 */
@Composable
fun Avatar(
    name: String,
    url: String?,
    images: AvatarImages?,
    modifier: Modifier = Modifier,
    size: Dp = ArcSizes.avatar,
    guest: Boolean = false,
    initialsSize: TextUnit = TextUnit.Unspecified,
) {
    val c = Stretch.colors
    val image = if (guest) null else images?.image(url)
    Box(
        modifier.size(size).clip(CircleShape).background(if (guest) Arc.colors.guestWash else c.surfaceControlHi),
        contentAlignment = Alignment.Center,
    ) {
        when {
            guest -> Icon(ArcIcons.Guest, Arc.colors.guest, size = ArcSizes.ring)
            image != null -> Image(image, name, Modifier.size(size), contentScale = ContentScale.Crop)
            else -> BasicText(
                Initials.of(name),
                style = Stretch.type.chip.merge(TextStyle(color = c.ink, fontWeight = FontWeight.SemiBold, fontSize = initialsSize, lineHeight = initialsSize)),
            )
        }
    }
}
