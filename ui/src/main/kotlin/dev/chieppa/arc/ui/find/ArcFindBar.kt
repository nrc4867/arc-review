package dev.chieppa.arc.ui.find

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.find_close
import dev.chieppa.arc.design.generated.find_count
import dev.chieppa.arc.design.generated.find_match_case
import dev.chieppa.arc.design.generated.find_match_case_glyph
import dev.chieppa.arc.design.generated.find_next
import dev.chieppa.arc.design.generated.find_none
import dev.chieppa.arc.design.generated.find_placeholder
import dev.chieppa.arc.design.generated.find_previous
import dev.chieppa.arc.design.generated.find_regex
import dev.chieppa.arc.design.generated.find_regex_glyph
import dev.chieppa.arc.design.generated.find_whole_word
import dev.chieppa.arc.design.generated.find_whole_word_glyph
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.TextField
import dev.chieppa.stretch.ui.framedSurface
import org.jetbrains.compose.resources.stringResource

/**
 * The find bar that floats over a pane: the query, match case, whole word and regular expression, which match is
 * current of how many, moving between them and closing. Enter goes to the next match, Shift Enter the previous, Esc
 * closes. A query that finds nothing, or a regular expression that does not parse, is edged in the error color.
 */
@Composable
fun ArcFindBar(find: FindState, onMove: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val focus = remember { FocusRequester() }
    LaunchedEffect(find.opened) { focus.requestFocus() }
    val query = find.query
    val none = query.text.isNotEmpty() && (find.invalid || find.matches.isEmpty())
    Row(
        modifier
            .width(ArcSizes.findWidth)
            .framedSurface(Stretch.shapes.cardShape, c.surfaceFloat, c.lineStrong, Stretch.stroke)
            .padding(horizontal = StretchSpacing.space6, vertical = StretchSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            query.text,
            { find.query = query.copy(text = it) },
            Modifier
                .width(ArcSizes.findField)
                .focusRequester(focus)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Enter, Key.NumPadEnter -> onMove(if (event.isShiftPressed) -1 else 1)
                        Key.Escape -> find.open = false
                        else -> return@onPreviewKeyEvent false
                    }
                    true
                },
            placeholder = stringResource(Res.string.find_placeholder),
            icon = StretchIcons.Search,
            error = if (none) "" else null,
        )
        GlyphToggle(stringResource(Res.string.find_match_case_glyph), stringResource(Res.string.find_match_case), query.matchCase, { find.query = query.copy(matchCase = it) })
        GlyphToggle(stringResource(Res.string.find_whole_word_glyph), stringResource(Res.string.find_whole_word), query.wholeWord, { find.query = query.copy(wholeWord = it) })
        GlyphToggle(stringResource(Res.string.find_regex_glyph), stringResource(Res.string.find_regex), query.regex, { find.query = query.copy(regex = it) })
        val count = when {
            query.text.isEmpty() -> ""
            none -> stringResource(Res.string.find_none)
            else -> stringResource(Res.string.find_count, find.current + 1, find.matches.size)
        }
        BasicText(count, Modifier.weight(1f), style = Stretch.type.valueSm.merge(TextStyle(color = if (none) c.errorInk else c.inkMuted)), maxLines = 1)
        IconButton(StretchIcons.ArrowUp, stringResource(Res.string.find_previous), { onMove(-1) }, size = StretchSizes.seg)
        IconButton(StretchIcons.ArrowDown, stringResource(Res.string.find_next), { onMove(1) }, size = StretchSizes.seg)
        IconButton(StretchIcons.Close, stringResource(Res.string.find_close), { find.open = false }, size = StretchSizes.seg)
    }
}
