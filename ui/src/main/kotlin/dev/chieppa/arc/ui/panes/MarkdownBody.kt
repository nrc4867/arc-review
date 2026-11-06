package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.stretch.markdown.MarkdownEditor
import dev.chieppa.stretch.markdown.MarkdownEditorState
import dev.chieppa.stretch.markdown.MarkdownMode

/** A Markdown file drawn as a page, read-only, by Stretch's Markdown editor. */
@Composable
fun MarkdownBody(text: String, modifier: Modifier = Modifier) {
    val state = remember(text) { MarkdownEditorState(text, MarkdownMode.Wysiwyg) }
    MarkdownEditor(state, modifier, modes = listOf(MarkdownMode.Wysiwyg), readOnly = true, showToolbar = false)
}
