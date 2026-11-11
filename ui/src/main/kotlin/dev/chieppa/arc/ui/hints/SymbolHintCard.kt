package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.stretch.code.SymbolCard
import dev.chieppa.stretch.markdown.MarkdownView
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.stretchShadow
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.framedSurface

/**
 * The card board 6b draws over a name the pointer rests on: the definition's head and signature, its doc comment read,
 * and its foot. A name with several definitions steps between them. Parts the definition's source changed are washed
 * as added. An answer with no definitions shows its Markdown alone.
 */
@Composable
fun SymbolHintCard(answer: HoverAnswer, card: SymbolCard, hints: HintController) {
    val shape = Stretch.shapes.cardShape
    val c = Stretch.colors
    Column(
        Modifier.width(HintSizes.width).stretchShadow(Stretch.shadows.pop, shape).framedSurface(shape, c.surfaceFloat, c.lineStrong, Stretch.stroke),
    ) {
        if (answer.symbols.isEmpty()) {
            MarkdownView(answer.markdown, Modifier.padding(horizontal = HintSizes.padH, vertical = HintSizes.padV))
            return@Column
        }
        var index by remember(answer) { mutableIntStateOf(0) }
        val hint = answer.symbols[index.coerceIn(answer.symbols.indices)]
        val changed by produceState(emptySet<Int>(), hint) { value = hints.changed(hint) }
        HintHead(hint, changed, index, answer.symbols.size) { step -> index = (index + step).coerceIn(answer.symbols.indices) }
        hint.doc?.let { doc ->
            Divider()
            HintDocBody(doc, changed) { link -> hints.follow(link, hint) }
        }
        Divider()
        HintFooter(hint, card.pinned, onPin = { card.pinned = !card.pinned }) {
            card.close()
            hints.open(hint)
        }
    }
}
