package dev.chieppa.arc.ui.hints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.codeintel.model.HintEntry
import dev.chieppa.arc.codeintel.model.HintSection
import dev.chieppa.stretch.markdown.MarkdownView
import dev.chieppa.stretch.theme.Stretch

/**
 * One section of a doc comment: its label in a column of its own, then each tag, its name in code and its text, a tag
 * the source's change wrote ([changed]) washed as added.
 */
@Composable
fun HintSectionRow(section: HintSection, changed: Set<Int>, onLink: (String) -> Unit) {
    val c = Stretch.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HintSizes.nameGap)) {
        BasicText(HintLabels.of(section), Modifier.width(HintSizes.labelColumn), style = Stretch.type.eyebrow.merge(TextStyle(color = c.inkMuted)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HintSizes.gap / 2)) {
            section.entries.forEach { HintEntryRow(it, if (it.line in changed) c.diffAddWash else Color.Unspecified, onLink) }
        }
    }
}

@Composable
private fun HintEntryRow(entry: HintEntry, wash: Color, onLink: (String) -> Unit) {
    val c = Stretch.colors
    val name = entry.name
    Row(
        Modifier.fillMaxWidth().background(wash, Stretch.shapes.segShape),
        horizontalArrangement = Arrangement.spacedBy(HintSizes.nameGap),
        verticalAlignment = Alignment.Top,
    ) {
        if (name != null) {
            BasicText(
                name,
                Modifier.background(c.surfaceControl, Stretch.shapes.segShape).padding(horizontal = HintSizes.gap / 2),
                style = Stretch.type.valueSm.merge(TextStyle(color = c.inkStrong)),
            )
        }
        if (entry.markdown.isNotBlank()) MarkdownView(entry.markdown, Modifier.weight(1f), onLinkClick = onLink)
    }
}
