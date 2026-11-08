package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Tooltip

/** The primary button under what is being written, with the keys that do the same in its tooltip. */
@Composable
fun SubmitButton(text: String, onClick: () -> Unit, icon: ImageVector) {
    Tooltip(text, ArcKeys.SUBMIT_COMMENT.caps()) { Button(text, onClick, kind = ButtonKind.Primary, icon = icon) }
}
