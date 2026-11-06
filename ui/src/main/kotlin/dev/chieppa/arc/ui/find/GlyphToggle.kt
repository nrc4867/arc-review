package dev.chieppa.arc.ui.find

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.ui.Tooltip

/** A square on-or-off button showing a short [glyph], such as `Aa` for match case, named by [description]. */
@Composable
fun GlyphToggle(glyph: String, description: String, on: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    Tooltip(description) {
        Box(
            modifier
                .size(StretchSizes.seg)
                .clip(shape)
                .background(if (on) c.onFill else Color.Transparent)
                .border(Stretch.stroke, if (on) c.onEdge else Color.Transparent, shape)
                .toggleable(on, role = Role.Checkbox, onValueChange = onChange)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            BasicText(glyph, style = Stretch.type.valueSm.merge(TextStyle(color = if (on) c.onInk else c.inkControl, fontWeight = FontWeight.SemiBold)))
        }
    }
}
