package dev.chieppa.arc.ui.hints

import dev.chieppa.arc.codeintel.model.HintDoc

/** Which doc comments a pane draws rendered. */
object DocBlocks {
    /**
     * The docs of [docs] to draw rendered: all but those with a line in [kept], such as lines the change wrote, lines
     * with threads or find matches, and those shown as written by a click ([raw], by first line).
     */
    fun shown(docs: List<HintDoc>, kept: Set<Int>, raw: Set<Int>): List<HintDoc> =
        docs.filter { doc -> doc.firstLine !in raw && (doc.firstLine..doc.lastLine).none { it in kept } }
}
