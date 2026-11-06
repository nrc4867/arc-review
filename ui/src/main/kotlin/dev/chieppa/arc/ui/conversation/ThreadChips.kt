package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_addressed
import dev.chieppa.arc.design.generated.thread_draft
import dev.chieppa.arc.design.generated.thread_guest
import dev.chieppa.arc.design.generated.thread_open
import dev.chieppa.arc.design.generated.thread_resolved
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.stringResource

/** The label and icon a thread's state chip shows. */
@Composable
fun threadChip(state: ThreadState): Pair<String, ImageVector> = when (state) {
    ThreadState.Open -> stringResource(Res.string.thread_open) to ArcIcons.Comment
    ThreadState.Addressed -> stringResource(Res.string.thread_addressed) to ArcIcons.CommentMoved
    ThreadState.Resolved -> stringResource(Res.string.thread_resolved) to ArcIcons.CommentCheck
    ThreadState.Draft -> stringResource(Res.string.thread_draft) to StretchIcons.Pen
    ThreadState.Guest -> stringResource(Res.string.thread_guest) to ArcIcons.Guest
}
