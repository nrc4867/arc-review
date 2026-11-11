package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.codeintel.model.HintDoc

/** A symbol card's doc comment, scrolling past [HintSizes.docHeight]. */
@Composable
fun HintDocBody(doc: HintDoc, changed: Set<Int>, onLink: (String) -> Unit) {
    HintDocContent(
        doc,
        changed,
        onLink,
        Modifier.fillMaxWidth().heightIn(max = HintSizes.docHeight).verticalScroll(rememberScrollState())
            .padding(horizontal = HintSizes.padH, vertical = HintSizes.padV),
    )
}
