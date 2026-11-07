package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_image_after
import dev.chieppa.arc.design.generated.pane_image_before
import dev.chieppa.arc.design.generated.pane_image_size
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ImageView
import dev.chieppa.stretch.ui.VerticalDivider
import org.jetbrains.compose.resources.stringResource

/**
 * A picture file drawn: changed, the picture before and after side by side, each under a bar in the removed or added
 * color; otherwise the one picture there is. Each shows its size in pixels under it.
 */
@Composable
fun ImageBody(text: PaneText, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val old = text.oldImage
    val new = text.newImage
    if (old != null && new != null) {
        Row(modifier.background(c.surfacePanel)) {
            Picture(old, stringResource(Res.string.pane_image_before), c.diffDelStrong, Modifier.weight(1f))
            VerticalDivider()
            Picture(new, stringResource(Res.string.pane_image_after), c.diffAddStrong, Modifier.weight(1f))
        }
    } else {
        (new ?: old)?.let { Picture(it, null, Color.Unspecified, modifier) }
    }
}

@Composable
private fun Picture(image: ImageBitmap, description: String?, bar: Color, modifier: Modifier) {
    val c = Stretch.colors
    Column(modifier.fillMaxHeight().background(c.surfacePanel)) {
        if (bar != Color.Unspecified) Box(Modifier.fillMaxWidth().height(ArcSizes.imageSideBar).background(bar))
        ImageView(image, Modifier.weight(1f).fillMaxWidth(), description)
        BasicText(
            stringResource(Res.string.pane_image_size, image.width, image.height),
            Modifier.align(Alignment.CenterHorizontally).padding(StretchSpacing.space8),
            style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)),
        )
    }
}
