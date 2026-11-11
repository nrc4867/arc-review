package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc

/** A doc comment drawn in place of its lines, as board 6c draws it; a click shows it as written again. */
@Composable
fun RenderedDocBlock(doc: HintDoc, document: DocumentRef, hints: HintController, onRaw: () -> Unit) {
    HintDocContent(
        doc,
        emptySet(),
        { link -> hints.follow(link, document) },
        Modifier.widthIn(max = HintSizes.blockWidth).fillMaxWidth().clickable(onClick = onRaw).padding(end = HintSizes.padH),
    )
}
