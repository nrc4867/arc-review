package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.hint_container
import dev.chieppa.arc.design.generated.hint_count
import dev.chieppa.arc.design.generated.hint_next
import dev.chieppa.arc.design.generated.hint_previous
import dev.chieppa.arc.ui.navigation.SymbolIcons
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.IconButton
import org.jetbrains.compose.resources.stringResource

/**
 * The card's head: the definition's icon and what it is inside (or its file when at the top), the steps between
 * definitions when a name has several ([index] of [count]), and its signature.
 */
@Composable
fun HintHead(hint: SymbolHint, changed: Set<Int>, index: Int, count: Int, onStep: (Int) -> Unit) {
    val c = Stretch.colors
    Column(
        Modifier.fillMaxWidth().background(c.surfacePanel).padding(horizontal = HintSizes.padH, vertical = HintSizes.padV),
        verticalArrangement = Arrangement.spacedBy(HintSizes.gap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HintSizes.gap)) {
            Icon(SymbolIcons.of(hint.target.kind), c.inkMuted, size = HintSizes.smallIcon)
            val container = hint.container
            val where = if (container != null && hint.containerKind != null) {
                stringResource(Res.string.hint_container, hint.containerKind.orEmpty(), container)
            } else {
                container ?: hint.target.location.document.path
            }
            BasicText(where, Modifier.weight(1f), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (count > 1) {
                IconButton(StretchIcons.ChevronLeft, stringResource(Res.string.hint_previous), { onStep(-1) }, size = HintSizes.button, enabled = index > 0)
                BasicText(stringResource(Res.string.hint_count, index + 1, count), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
                IconButton(StretchIcons.ChevronRight, stringResource(Res.string.hint_next), { onStep(1) }, size = HintSizes.button, enabled = index < count - 1)
            }
        }
        HintSignature(hint, changed)
    }
}
