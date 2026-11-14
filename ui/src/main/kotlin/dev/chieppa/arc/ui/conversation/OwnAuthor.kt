package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_you
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.stringResource

/** The person as their own comments, replies and notes show them: [name] for their initials and [avatar], their picture. */
data class OwnAuthor(val name: String, val avatar: String?)

/**
 * The person signed in to GitHub, by their name or else their login, with their picture; the login [source]'s
 * conversation names while that is not known yet, and "You" with no picture when neither is.
 */
@Composable
fun ownAuthor(workbench: Workbench, source: Source): OwnAuthor {
    val viewer = workbench.watch.viewer
    val name = viewer?.name ?: viewer?.login ?: workbench.comments.controller.viewer(source) ?: stringResource(Res.string.comment_you)
    return OwnAuthor(name, viewer?.avatarUrl)
}
