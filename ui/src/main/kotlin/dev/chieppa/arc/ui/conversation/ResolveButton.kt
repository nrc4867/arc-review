package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.thread_resolve
import dev.chieppa.arc.design.generated.thread_unresolve
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.stringResource

/** Resolves a host's thread, or opens a resolved one again; nothing for a thread that is not the host's. */
@Composable
fun ResolveButton(thread: ReviewThread, controller: ConversationController) {
    if (!thread.hosted) return
    val resolved = thread.state == ThreadState.Resolved
    Button(
        stringResource(if (resolved) Res.string.thread_unresolve else Res.string.thread_resolve),
        { controller.resolve(thread, !resolved) },
        kind = ButtonKind.Quiet,
        icon = if (resolved) ArcIcons.Comment else StretchIcons.Check,
    )
}
