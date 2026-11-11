package dev.chieppa.arc.ui.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.stretch.code.SymbolCard

/**
 * The symbol card for the name at [line], [column] of [document], as a pane's viewer opens it: nothing until the
 * answer comes, and nothing when there is none.
 */
@Composable
fun PaneHintCard(document: DocumentRef, line: Int, column: Int, card: SymbolCard, hints: HintController) {
    val answer = produceState<HoverAnswer?>(null, document, line, column) { value = hints.hover(document, line, column) }.value ?: return
    SymbolHintCard(answer, card, hints)
}
