package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.stretch.frame.chord
import dev.chieppa.stretch.ui.TextArea

/**
 * The text of a comment, reply or note being written. Ctrl Enter runs [onSubmit] and Esc runs [onCancel]; it takes the
 * keyboard when it opens if [focus] says so.
 */
@Composable
fun CommentComposer(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    focus: Boolean = true,
    minLines: Int = COMPOSER_LINES,
) {
    val requester = remember { FocusRequester() }
    TextArea(
        text,
        onTextChange,
        modifier
            .fillMaxWidth()
            .focusRequester(requester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.chord()) {
                    ArcKeys.SUBMIT_COMMENT -> onSubmit().let { true }
                    ArcKeys.CANCEL_COMMENT -> onCancel().let { true }
                    else -> false
                }
            },
        placeholder = placeholder,
        minLines = minLines,
    )
    LaunchedEffect(Unit) { if (focus) requester.requestFocus() }
}

/** How many lines a comment's text starts with. */
private const val COMPOSER_LINES = 2
