package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import dev.chieppa.arc.codeintel.model.HintLine
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.stretch.code.CodeColors
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.code.codeColors
import dev.chieppa.stretch.code.highlighted
import dev.chieppa.stretch.code.tokenizeLines
import dev.chieppa.stretch.theme.Stretch

/**
 * A hint's signature in code colours, its lines run together and wrapping: a line the source's change wrote ([changed])
 * washed as added code, and the name struck through when the definition is deprecated.
 */
@Composable
fun HintSignature(hint: SymbolHint, changed: Set<Int>, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val colors = codeColors
    val text = remember(hint, changed, colors, c.diffAddStrong) { build(hint, changed, colors, c.diffAddStrong) }
    BasicText(text, modifier, style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)))
}

private fun build(hint: SymbolHint, changed: Set<Int>, colors: CodeColors, wash: Color): AnnotatedString {
    val ranges = ArrayList<Pair<HintLine, IntRange>>()
    val joined = StringBuilder()
    for (line in hint.signature) {
        val closes = line.text.startsWith(")") || line.text.startsWith("]")
        if (closes && joined.endsWith(",")) joined.setLength(joined.length - 1)
        if (joined.isNotEmpty() && !joined.endsWith("(") && !joined.endsWith("[") && !line.text.startsWith(")") && !line.text.startsWith("]")) {
            joined.append(' ')
        }
        val start = joined.length
        joined.append(line.text)
        ranges += line to (start until joined.length)
    }
    val text = joined.toString()
    val tokens = tokenizeLines(listOf(text), CodeLanguages.forPath(hint.target.location.document.path)).first()
    return buildAnnotatedString {
        append(highlighted(text, tokens, colors))
        for ((line, range) in ranges) if (line.line in changed && !range.isEmpty()) addStyle(SpanStyle(background = wash), range.first, range.last + 1)
        if (hint.doc?.deprecated != null) {
            val at = Regex("\\b${Regex.escape(hint.target.name)}\\b").find(text)?.range
            if (at != null) addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), at.first, at.last + 1)
        }
    }
}
