package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.hint_deprecated
import dev.chieppa.stretch.markdown.MarkdownView
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * A doc comment, read: its deprecation note in the warning ink, its description, and its sections. The description
 * is washed as added when the source's change wrote any of its lines ([changed]). Scrolls past [HintSizes.docHeight].
 */
@Composable
fun HintDocBody(doc: HintDoc, changed: Set<Int>, onLink: (String) -> Unit) {
    val c = Stretch.colors
    val descriptionEnd = doc.sections.flatMap { it.entries }.minOfOrNull { it.line }?.minus(1) ?: doc.lastLine
    val descriptionChanged = (doc.firstLine..descriptionEnd).any { it in changed }
    Column(
        Modifier.fillMaxWidth().heightIn(max = HintSizes.docHeight).verticalScroll(rememberScrollState())
            .padding(horizontal = HintSizes.padH, vertical = HintSizes.padV),
        verticalArrangement = Arrangement.spacedBy(HintSizes.padV),
    ) {
        doc.deprecated?.let { note ->
            Row(horizontalArrangement = Arrangement.spacedBy(HintSizes.gap)) {
                BasicText(stringResource(Res.string.hint_deprecated), style = Stretch.type.body.merge(TextStyle(color = c.warnInk)))
                if (note.isNotBlank()) MarkdownView(note, Modifier.weight(1f), onLinkClick = onLink)
            }
        }
        if (doc.description.isNotBlank()) {
            MarkdownView(
                doc.description,
                Modifier.fillMaxWidth().background(if (descriptionChanged) c.diffAddWash else Color.Unspecified, Stretch.shapes.segShape),
                onLinkClick = onLink,
            )
        }
        doc.sections.forEach { HintSectionRow(it, changed, onLink) }
    }
}
