package dev.chieppa.arc.ui.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.ui.workbench.Workbench

/** The doc comments a pane draws rendered ([shown], top to bottom), and how to show one as written again. */
class PaneDocs(val shown: List<HintDoc>, val showRaw: (HintDoc) -> Unit) {
    /** The doc whose rendering starts at [line], if one does. */
    fun at(line: Int): HintDoc? = shown.firstOrNull { it.firstLine == line }

    /** Each shown doc's lines. */
    val ranges: List<IntRange> get() = shown.map { it.firstLine..it.lastLine }
}

/**
 * The doc comments of [document], whose content is [text], that pane [paneId] draws rendered while its Rendered docs
 * toggle is on: all but those with a line in [kept] and those a click showed as written, until the toggle is used.
 */
@Composable
fun rememberPaneDocs(workbench: Workbench, paneId: String, document: DocumentRef, text: String?, kept: Set<Int>): PaneDocs {
    val on = workbench.state.paneState(paneId).docs
    val raw = remember(paneId, on) { mutableStateListOf<Int>() }
    val docs = produceState(emptyList<HintDoc>(), on, document, text) {
        value = if (on && text != null) workbench.hints.docs(document, text) else emptyList()
    }.value
    val rawLines = raw.toSet()
    val shown = remember(docs, kept, rawLines) { DocBlocks.shown(docs, kept, rawLines) }
    return PaneDocs(shown) { raw += it.firstLine }
}
