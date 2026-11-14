package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.core.activity.failureHeadline
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pull_clone_failed
import dev.chieppa.arc.design.generated.pull_clone_failed_because
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The error mark on a row whose repo [repoName] could not be cloned: its tooltip says why ([reason]'s telling line, git's
 * `fatal:` one), and a click runs [onClick], which opens the Console where the whole reason is.
 */
@Composable
fun CloneFailedMark(repoName: String, reason: String?, onClick: () -> Unit) {
    val headline = reason?.let(::failureHeadline)?.takeIf { it.isNotEmpty() }
    val text = if (headline != null) stringResource(Res.string.pull_clone_failed_because, repoName, headline) else stringResource(Res.string.pull_clone_failed, repoName)
    Tooltip(text) {
        Icon(
            StretchIcons.Error,
            Stretch.colors.errorInk,
            Modifier.pointerHoverIcon(PointerIcon.Hand).clickable(role = Role.Button, onClickLabel = text, onClick = onClick),
            contentDescription = text,
        )
    }
}
