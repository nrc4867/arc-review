package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.stretch.frame.MatchedText
import dev.chieppa.stretch.frame.fuzzyMatch
import dev.chieppa.stretch.theme.Stretch

/** A switcher row's title in the strong ink, with what [query] matched in it marked. */
@Composable
fun RowTitle(text: String, query: String, modifier: Modifier = Modifier) {
    MatchedText(text, fuzzyMatch(query, text)?.ranges.orEmpty(), Stretch.type.body, Stretch.colors.inkStrong, modifier)
}
