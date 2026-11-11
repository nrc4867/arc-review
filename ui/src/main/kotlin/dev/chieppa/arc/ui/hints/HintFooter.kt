package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.hint_click_to_go
import dev.chieppa.arc.design.generated.hint_keep_open
import dev.chieppa.arc.design.generated.hint_origin
import dev.chieppa.arc.design.generated.navigation_by_name
import dev.chieppa.arc.design.generated.navigation_exact
import dev.chieppa.arc.design.generated.navigation_line
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Kbd
import org.jetbrains.compose.resources.stringResource

/**
 * The card's foot: where the definition is (a click goes there) and the revision it was read at, the doc comment's
 * convention and how the definition was found, the pin that keeps the card open, and the click that goes there.
 */
@Composable
fun HintFooter(hint: SymbolHint, pinned: Boolean, onPin: () -> Unit, onOpen: () -> Unit) {
    val c = Stretch.colors
    val target = hint.target
    val document = target.location.document
    val small = Stretch.type.valueSm.merge(TextStyle(color = c.ink))
    Row(
        Modifier.fillMaxWidth().background(c.surfacePanel)
            .padding(start = HintSizes.footerStart, end = HintSizes.footerEnd, top = HintSizes.footerV, bottom = HintSizes.footerV),
        horizontalArrangement = Arrangement.spacedBy(HintSizes.nameGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val place = buildAnnotatedString {
            append(document.path)
            withStyle(SpanStyle(color = c.inkSubtle)) { append(stringResource(Res.string.navigation_line, target.location.range.start.line)) }
        }
        BasicText(place, Modifier.weight(1f, fill = false).clickable(onClick = onOpen), style = small, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(target.source ?: document.revision.take(SHORT_SHA), style = small.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        Row(Modifier.weight(1f)) {}
        val precision = stringResource(if (target.origin.precision == Precision.Exact) Res.string.navigation_exact else Res.string.navigation_by_name)
        val origin = hint.doc?.let { stringResource(Res.string.hint_origin, HintLabels.format(it.format), precision) } ?: precision
        BasicText(origin, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1)
        IconButton(ArcIcons.Pin, stringResource(Res.string.hint_keep_open), onPin, size = HintSizes.button, on = pinned)
        Kbd(HintLabels.clickModifier())
        BasicText(stringResource(Res.string.hint_click_to_go), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
    }
}

/** How many characters of a commit sha the foot shows. */
private const val SHORT_SHA = 7
