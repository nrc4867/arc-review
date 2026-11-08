package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.navigation_line
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.code.codeColors
import dev.chieppa.stretch.code.highlighted
import dev.chieppa.stretch.code.tokenizeLines
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * One place in a navigation list: its path and line, and the line's code coloured with the name set off. A filtered
 * place draws [dimmed].
 */
@Composable
fun TargetRow(target: SymbolTarget, dimmed: Boolean, onOpen: (SymbolTarget) -> Unit) {
    val c = Stretch.colors
    val location = target.location
    val start = location.range.start
    val colors = codeColors
    val highlight = c.warnWash
    val code = remember(target, colors, highlight) {
        val line = target.preview
        val indent = line.length - line.trimStart().length
        val shown = line.trimStart()
        val tokens = tokenizeLines(listOf(shown), CodeLanguages.forPath(location.document.path)).first()
        val coloured = highlighted(shown, tokens, colors)
        val from = (start.column - indent).coerceIn(0, shown.length)
        val to = (location.range.end.column - indent).coerceIn(from, shown.length)
        buildAnnotatedString {
            append(coloured)
            if (to > from) addStyle(SpanStyle(background = highlight), from, to)
        }
    }
    PaletteRow(false, { onOpen(target) }, Modifier.heightIn(min = NavigationSizes.row).alpha(if (dimmed) NavigationSizes.FILTERED_ALPHA else 1f)) {
        val path = buildAnnotatedString {
            append(location.document.path)
            withStyle(SpanStyle(color = c.inkSubtle)) { append(stringResource(Res.string.navigation_line, start.line)) }
        }
        Row(Modifier.width(NavigationSizes.pathColumn)) {
            BasicText(path, style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        BasicText(code, Modifier.weight(1f), style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
