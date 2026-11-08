package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch

/** What a comment says, as its author wrote it. */
@Composable
fun CommentBody(text: String, workbench: Workbench, modifier: Modifier = Modifier) {
    SelectionContainer(modifier) {
        BasicText(text, style = Stretch.type.body.merge(TextStyle(color = Stretch.colors.ink)))
    }
}
